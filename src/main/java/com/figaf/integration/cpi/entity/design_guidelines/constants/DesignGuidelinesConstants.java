package com.figaf.integration.cpi.entity.design_guidelines.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DesignGuidelinesConstants {

    public static Map<String, String> RULE_LABELS = initializeMap();

    private static Map<String, String> initializeMap() {
        Map<String, String> ruleLabels = new HashMap<>();
        ruleLabels.put("DEFINE_PROPER_TRANSACTION_HANDLING", "Define Proper Transaction Handling");
        ruleLabels.put("TRANSACTIONAL_PROCESSING_SET_FOR_PARALLEL_PROCESSING", "Transactional processing set for Parallel Processing");
        ruleLabels.put("AVOID_MIXING_JDBC_AND_JMS_TRANSACTIONS", "Avoid mixing JDBC and JMS transactions");
        ruleLabels.put("CONTROL_THE_NUMBER_OF_SIMULTANEOUSLY_OPENED_DATABASE_CONNECTIONS", "Keep The Transactions Shorter");

        ruleLabels.put("HANDLE_ERRORS_GRACEFULLY", "Handle Errors Gracefully");
        ruleLabels.put("CONTINUE_MESSAGE_PROCESSING_EVEN_AFTER_AN_EXCEPTION", "Handle Exceptions in Local Integration Process");
        ruleLabels.put("HANDLE_EXCEPTIONS", "Handle Exceptions");

        ruleLabels.put("ANTICIPATE_MESSAGE_THROUGHPUT_WHEN_CHOOSING_A_STORAGE_OPTION", "Anticipate Message Throughput When Choosing a Storage Option");
        ruleLabels.put("UNEXPECTED_DATA_GROWTH", "Unexpected Data Growth");

        ruleLabels.put("RUN_AN_INTEGRATION_FLOW_UNDER_WELL_DEFINED_BOUNDARY_CONDITIONS", "Run an Integration Flow Under Well-Defined Boundary Conditions");
        ruleLabels.put("LIMIT_SIZE_OF_INCOMING_MESSAGES", "Limit Size of Incoming Messages");
        ruleLabels.put("SUCCESSFACTORS_API_INFRASTRUCTURE_TIMEOUT", "SuccessFactors API infrastructure Timeout");
        ruleLabels.put("MANAGE_LARGE_BATCH_SIZES", "Manage Large Batch Sizes");
        ruleLabels.put("MANAGE_TIMEOUT_FOR_SUCCESSFACTORS", "Manage Timeout For SuccessFactors");
        ruleLabels.put("COMMON_ROLE_FOR_SENDER_AUTHORIZATION", "Avoid Using a Generic User Role for Sender-Side Authorization");
        ruleLabels.put("OPTMIZE_INTEGRATION_FLOW_DESIGN_FOR_STREAMING", "Optimize Integration Flow Design For Streaming");

        ruleLabels.put("RELAX_DEPENDENCIES_TO_EXTERNAL_COMPONENTS", "Relax Dependencies to External Components");
        ruleLabels.put("APPLY_THE_RETRY_PATTERN_WITH_JMS_QUEUE", "Apply Retry Pattern With JMS Queue");

        ruleLabels.put("APPLY_THE_HIGHEST_SECURITY_STANDARDS", "Apply the Highest Security Standards");
        ruleLabels.put("USE_SECURE_AUTHENTICATION_METHODS", "Use Secure Authentication Methods");
        ruleLabels.put("USE_SECURE_PROTOCOLS", "Use Secure Protocols");
        ruleLabels.put("USE_CSRF_PROTECTION", "Use CSRF Protection");
        ruleLabels.put("UPLOAD_WSDLS_AS_INTEGRATION_FLOW_RESOURCE", "Upload WSDLs as Integration Flow Resources");

        ruleLabels.put("OPTIMIZE_MEMORY_FOOTPRINT", "Optimize Memory Footprint");
        ruleLabels.put("XPATH_CONDITIONS", "Use XPATH Condition Appropriately");
        ruleLabels.put("RESET_DATA_AFTER_MULTIPLE_BRANCHES_THROUGH_A_MULTICAST", "Reset Data For Every Branch");
        ruleLabels.put("USE_BYTE_ARRAY_AS_OUTPUT_TYPE", "Use ByteArray As Output Type To Process Large Messages");

        ruleLabels.put("KEEP_READABILITY_IN_MIND", "Keep Readability in Mind");
        ruleLabels.put("EXTERNALIZE_VOLATILE_CONFIGURATIONS_FOR_RECEIVER_ADAPTERS", "Externalize Volatile Configurations for Receiver Adapters");
        ruleLabels.put("EXTERNALIZE_VOLATILE_CONFIGURATIONS_FOR_SENDER_ADAPTERS", "Externalize Volatile Configurations for Sender Adapters");
        ruleLabels.put("SPECIAL_HEADER_VARIABLES_ENABLED_FOR_MONITORING", "Define Identifiers To Search In Message Processing Logs");
        ruleLabels.put("DETECT_ORPHAN_INTEGRATION_FLOW_PIPELINE_STEP", "Remove Unused Integration Flow Steps");
        ruleLabels.put("VALIDATION_CHECK_ON_INTEGRATION_FLOW_FOR_VERSION_INCOMPATIBILITIES", "Always Use Latest Version of Flow Steps");
        ruleLabels.put("OUTSOURCE_INTEGRATION_LOGIC_INTO_SEPERATE_INTEGRATION_FLOWS", "Use Only Allowed Number of Pools and Flow Steps");

        ruleLabels.put("GENERAL_SCRIPTING_GUIDELINES", "Scripting Guidelines");
        ruleLabels.put("EVAL_USAGE_CHECK", "Avoid Using Eval Classes");
        ruleLabels.put("AVOID_USING_TIMEZONE_SET_DEFAULT", "Avoid Using Default Timezone");
        ruleLabels.put("STREAM_THE_JSON_SLURPER_INPUT_IN_GROOVY_SCRIPTS", "Use of JsonSlurper");
        ruleLabels.put("STREAM_THE_XML_SLURPER_INPUT_IN_GROOVY_SCRIPTS", "Use of XMLSlurper");
        ruleLabels.put("SWITCH_OFF_RESOLVING_OF_EXTERNAL_ENTITIES", "Use of DocumentBuilderFactory without disallowing the doctype declaration");
        ruleLabels.put("AVOID_ACCESSING_SECURE_PARAMETER", "Avoid Accessing Secure Parameter");
        ruleLabels.put("AVOID_CREATING_MPL_ATTACHMENTS", "Avoid Creating MPL Attachments");
        ruleLabels.put("GROOVY_SCRIPT_STATIC_ANALYSIS", "Static analysis of Groovy scripts");
        ruleLabels.put("USE_ONLY_SUPPORTED_EXTERNAL_LIBRARIES", "Use Only Supported External Libraries");

        return Collections.unmodifiableMap(ruleLabels);
    }
}
