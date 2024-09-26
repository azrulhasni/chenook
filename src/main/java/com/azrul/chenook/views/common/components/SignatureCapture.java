package com.azrul.chenook.views.common.components;



import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import java.util.Base64;

@Tag("div")
@JsModule("./signature-pad.js")
public class SignatureCapture extends Div {

    public SignatureCapture() {
        setId("signature-pad");
        getElement().getStyle().set("border", "1px solid black");
        getElement().getStyle().set("width", "500px");
        getElement().getStyle().set("height", "200px");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().executeJs("initSignaturePad($0)", getElement());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        getElement().executeJs("destroySignaturePad($0)", getElement());
    }

    public void getSignatureAsImage(ImageReceiver receiver) {
        getElement().executeJs("return getSignatureImage()").then(String.class, base64 -> {
            if (base64 != null && !base64.isEmpty()) {
                String base64Data = base64.split(",")[1];
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                receiver.receiveImage(decodedBytes);
            } else {
                receiver.receiveImage(null);
            }
        });
    }

//     public byte[] getSignatureImage() {
//        final String[] result = new String[1];
//        getElement().executeJs("return getSignatureImage()").then(String.class, base64 -> {
//            VaadinSession.getCurrent().access(() -> {
//                result[0] = base64;
//                VaadinSession.getCurrent().lock();
//                try {
//                    VaadinSession.getCurrent().getSession().setAttribute("signatureImage", base64);
//                } finally {
//                    VaadinSession.getCurrent().unlock();
//                }
//            });
//        });
//
//       
//    }

    public void clearSignature() {
        getElement().executeJs("clearSignaturePad()");
    }

//    @FunctionalInterface
//    public interface ImageReceiver {
//        void receiveImage(StreamResource resource);
//    }
     @FunctionalInterface
    public interface ImageReceiver {
        void receiveImage(byte[] imageBinary);
    }
}