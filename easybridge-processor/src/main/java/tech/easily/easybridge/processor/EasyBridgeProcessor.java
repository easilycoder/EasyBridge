package tech.easily.easybridge.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import tech.easily.easybridge.annotation.EasyBridgeHandler;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"tech.easily.easybridge.annotation.EasyBridgeHandler"})
public class EasyBridgeProcessor extends AbstractProcessor {

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        List<BridgeHandlerModel> handlerModelList = new ArrayList<>();
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(EasyBridgeHandler.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            EasyBridgeHandler jsHandler = annotatedElement.getAnnotation(EasyBridgeHandler.class);
            handlerModelList.add(new BridgeHandlerModel(jsHandler.name(), annotatedClass.getQualifiedName().toString()));
        }
        JavaFile javaFile = EBHandlerManagerGenerator.brewJava(handlerModelList);
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
