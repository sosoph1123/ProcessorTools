package com.lzh.processor.compiler;

import com.google.auto.service.AutoService;
import com.lzh.processor.annoapi.Params;
import com.lzh.processor.util.FileLog;
import com.lzh.processor.util.UtilMgr;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class Compiler extends AbstractProcessor {

    private UtilMgr mgr;
    private boolean isFirst;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Params.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        UtilMgr mgr = UtilMgr.getMgr();
        mgr.setElementUtils(processingEnv.getElementUtils());
        mgr.setFiler(processingEnv.getFiler());
        mgr.setMessager(processingEnv.getMessager());
        mgr.setTypeUtils(processingEnv.getTypeUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isFirst) {
            return false;
        }
        isFirst = true;
        FileLog fileLog = new FileLog();
        FileLog.reload();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Params.class);
        for (Element ele : elements) {
            try {
                ElementParser parser = ElementParser.createParser((TypeElement) ele);
                JavaFactory javaFactory = new JavaFactory(parser);
                javaFactory.generateCode();
            }catch (Throwable e) {
                FileLog.printException(e);
                error(ele, "processor tool generate java files failed: %s,%s", ele, e.getMessage());
            }

        }

        return false;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
