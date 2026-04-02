#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include "llama-grammar.h"

extern "C"
JNIEXPORT jobject JNICALL
Java_net_amazingapps_llama_android_core_guidance_GbnfGrammar_nativeValidate(JNIEnv *env, jobject thiz, jstring jgrammar) {
    const char *grammar_str = env->GetStringUTFChars(jgrammar, nullptr);
    std::string grammar_std_str(grammar_str);
    env->ReleaseStringUTFChars(jgrammar, grammar_str);

    jclass successClass = env->FindClass("net/amazingapps/llama/android/core/guidance/GrammarValidationResult$Success");
    jclass failureClass = env->FindClass("net/amazingapps/llama/android/core/guidance/GrammarValidationResult$Failure");
    
    jmethodID failureConstructor = env->GetMethodID(failureClass, "<init>", "(Ljava/lang/String;I)V");
    jfieldID successInstanceField = env->GetStaticFieldID(successClass, "INSTANCE", "Lnet/amazingapps/llama/android/core/guidance/GrammarValidationResult$Success;");
    jobject successInstance = env->GetStaticObjectField(successClass, successInstanceField);

    llama_grammar_parser parser(nullptr);
    const char * src = grammar_std_str.c_str();
    
    try {
        const char * pos = src;
        
        while (pos && *pos) {
            // Skip spaces and comments
            while (*pos && (isspace(*pos) || *pos == '#')) {
                if (*pos == '#') {
                    while (*pos && *pos != '\r' && *pos != '\n') pos++;
                } else {
                    pos++;
                }
            }
            if (!*pos) break;
            
            pos = parser.parse_rule(pos);
        }

        // Validate that all rules are defined
        for (uint32_t i = 0; i < parser.rules.size(); i++) {
            if (parser.rules[i].empty()) {
                std::string name = "unknown";
                for (const auto & kv : parser.symbol_ids) {
                    if (kv.second == i) { name = kv.first; break; }
                }
                return env->NewObject(failureClass, failureConstructor, 
                    env->NewStringUTF(("Undefined rule: " + name).c_str()), -1);
            }
        }

        if (parser.symbol_ids.find("root") == parser.symbol_ids.end()) {
             return env->NewObject(failureClass, failureConstructor, 
                env->NewStringUTF("Grammar is missing 'root' rule"), 0);
        }

        auto * grammar = llama_grammar_init_impl(nullptr, grammar_std_str.c_str(), "root", false, nullptr, 0, nullptr, 0);
        if (grammar) {
            llama_grammar_free_impl(grammar);
            return successInstance;
        } else {
            return env->NewObject(failureClass, failureConstructor, 
                env->NewStringUTF("Validation failed (possible left recursion)"), -1);
        }

    } catch (const std::exception & err) {
        std::string msg = err.what();
        int offset = -1;
        
        size_t at_pos = msg.find(" at ");
        if (at_pos != std::string::npos) {
            std::string remaining = msg.substr(at_pos + 4);
            // We look for this exact suffix in our original string
            if (!remaining.empty()) {
                const char * remain_ptr = strstr(src, remaining.c_str());
                if (remain_ptr) {
                    offset = (int)(remain_ptr - src);
                }
            }
            msg = msg.substr(0, at_pos);
        }
        
        return env->NewObject(failureClass, failureConstructor, 
            env->NewStringUTF(msg.c_str()), offset);
    }
}
