package easily.tech.easybridge.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by lemon on 04/04/2018.
 */

public class EBHandlerManagerGenerator {

    private static final String CLASS_NAME = "EBHandlerManager";
    private static final String PACKAGE_NAME = "easily.tech.easybridge.lib";
    private static final String REGISTER_METHOD_NAME = "register";
    private static final String WEB_VIEW_PARAMETER = "webview";
    private static final String BRIDGE_WEB_VIEW_CLASS = PACKAGE_NAME + ".EasyBridgeWebView";

    public static JavaFile brewJava(List<BridgeHandlerModel> handlerModelList) {

        TypeSpec.Builder typeBuilder = classBuilder(CLASS_NAME).addModifiers(PUBLIC, FINAL);

        ClassName bridgeWebView = ClassName.bestGuess(BRIDGE_WEB_VIEW_CLASS);


        MethodSpec.Builder registerMethodBuilder = methodBuilder(REGISTER_METHOD_NAME)
                .addModifiers(PUBLIC, STATIC)
                .addParameter(bridgeWebView, WEB_VIEW_PARAMETER)
                .returns(TypeName.VOID);

        registerMethodBuilder.addCode("\n");
        if (handlerModelList != null && !handlerModelList.isEmpty()) {
            for (BridgeHandlerModel model : handlerModelList) {
                if (model == null) {
                    continue;
                }
                ClassName handlerClass = ClassName.bestGuess(model.className);
                String tempName = model.handlerName + "Handler";
                registerMethodBuilder.addStatement("$T $L = new $T($S,$L)", handlerClass, tempName, handlerClass, model.handlerName, WEB_VIEW_PARAMETER);
                registerMethodBuilder.addStatement("$L.registerHandler($L)", WEB_VIEW_PARAMETER, tempName);
                registerMethodBuilder.addCode("\n");
            }
        }

        typeBuilder.addJavadoc("using {@link #register(BridgeWebView)}to register the instance of BridgeHandler when init the webview of the page\n");

        typeBuilder.addMethod(registerMethodBuilder.build());

        return JavaFile.builder(PACKAGE_NAME, typeBuilder.build())
                .addFileComment("Generated code from EBHandlerManagerGenerator. Do not modify!")
                .build();
    }
}
