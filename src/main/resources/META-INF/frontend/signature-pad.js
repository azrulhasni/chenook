import SignaturePad from 'signature_pad';

let signaturePad;

window.initSignaturePad = (element) => {
//    const canvas = document.createElement('canvas');
//    canvas.width = '500px';
//    canvas.height = '200px';

//    element.appendChild(canvas);
    const canvas = document.createElement('canvas');
    element.appendChild(canvas);

    signaturePad = new SignaturePad(canvas);

    const resizeCanvas = () => {
        const ratio = 1;// Math.max(window.devicePixelRatio || 1, 1);
        canvas.width = element.clientWidth * ratio;
        canvas.height = element.clientHeight * ratio;
        canvas.getContext('2d').scale(ratio, ratio);
        signaturePad.clear(); // otherwise isEmpty() might return incorrect value
    };

    resizeCanvas(); // initially set canvas size

    const resizeObserver = new ResizeObserver(() => resizeCanvas());
    resizeObserver.observe(element);

    element.resizeObserver = resizeObserver;

    signaturePad = new SignaturePad(canvas);
    signaturePad.penColor = 'rgb(0,0,0)';
    signaturePad.addEventListener("beginStroke", () => {
        //console.log("Signature started");
    }, {once: false});
};

window.destroySignaturePad = (element) => {
    if (signaturePad) {
        signaturePad.off();
        signaturePad = null;
    }
};

window.getSignatureImage = () => {
    //console.log('Image:'+signaturePad.toDataURL());
    return signaturePad.toDataURL();
};

window.clearSignaturePad = () => {
    if (signaturePad) {
        signaturePad.clear();
    }
};