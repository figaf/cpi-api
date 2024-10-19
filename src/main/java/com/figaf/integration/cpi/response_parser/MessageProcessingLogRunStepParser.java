package com.figaf.integration.cpi.response_parser;

import com.figaf.integration.cpi.entity.message_processing.MessageProcessingLogRunStep;
import com.figaf.integration.cpi.entity.message_processing.MessageRunStepProperty;
import com.figaf.integration.cpi.entity.message_processing.PropertyType;
import com.figaf.integration.cpi.utils.CpiApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;

public class MessageProcessingLogRunStepParser {

    public static List<MessageProcessingLogRunStep> createMessageProcessingLogRunSteps(List<JSONObject> jsonObjectRunSteps) {
        List<MessageProcessingLogRunStep> runSteps = new ArrayList<>();
        for (int ind = jsonObjectRunSteps.size() - 1; ind >= 0; ind--) {
            JSONObject runStepElement = jsonObjectRunSteps.get(ind);

            MessageProcessingLogRunStep runStep = new MessageProcessingLogRunStep();
            runStep.setRunId(optString(runStepElement, "RunId"));
            runStep.setChildCount(runStepElement.getInt("ChildCount"));
            runStep.setStepStart(CpiApiUtils.parseDate(optString(runStepElement, "StepStart")));
            if (!runStepElement.isNull("StepStop")) {
                runStep.setStepStop(CpiApiUtils.parseDate(optString(runStepElement, "StepStop")));
            }
            runStep.setStepId(optString(runStepElement, "StepId"));
            runStep.setModelStepId(optString(runStepElement, "ModelStepId"));
            runStep.setBranchId(optString(runStepElement, "BranchId"));
            runStep.setStatus(optString(runStepElement, "Status"));
            runStep.setError(optString(runStepElement, "Error"));
            runStep.setActivity(optString(runStepElement, "Activity"));

            JSONArray runStepPropertiesJsonArray = runStepElement.getJSONObject("RunStepProperties").getJSONArray("results");
            String traceId = null;
            for (int runStepPropertyInd = 0; runStepPropertyInd < runStepPropertiesJsonArray.length(); runStepPropertyInd++) {
                JSONObject runStepPropertyElement = runStepPropertiesJsonArray.getJSONObject(runStepPropertyInd);

                String name = optString(runStepPropertyElement, "Name");
                String value = optString(runStepPropertyElement, "Value");
                //getRunStepProperties is not used anywhere
                runStep.getRunStepProperties().add(new MessageRunStepProperty(
                    PropertyType.RUN_STEP_PROPERTY,
                    name,
                    value
                ));
                if ("TraceIds".equals(name) && StringUtils.isNotBlank(value)) {
                    //This regex means that we want to find only the first value of the list
                    // since we rely on only one first trace message everywhere in the logic.
                    // In other words, we don't support multiple trace messages for a single run step
                    traceId = value.replaceAll("\\[(\\d*).*]", "$1");
                    if (StringUtils.isNotBlank(traceId)) {
                        runStep.setTraceId(traceId);
                    }
                }
            }

            //If traceId == null it means that this run step doesn't have payload. We don't need such messages at all.
            if (traceId != null) {
                runSteps.add(runStep);
            }
        }
        return runSteps;
    }
}
