const allowedFileTypes = [
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "text/plain"
];

export const calculateFileSize = (file) => {
    const sizeInBytes = file.size;
    const sizeInMB = sizeInBytes / (1024 * 1024);
    return sizeInMB;

}

export const isAllowedFileType = (fileTpye) => {
    return allowedFileTypes.includes(fileTpye);
}

export const fetchCssVariable = (variableName) => {
    return getComputedStyle(document.documentElement).getPropertyValue(variableName).trim();
};

export const addTextWithLetterSpacing = (doc, x, y, text, letterSpacing) => {
    let currentX = x;
    for (let i = 0; i < text.length; i++) {
        doc.text(text[i], currentX, y);  // Draw each letter
        currentX += doc.getStringUnitWidth(text[i]) * doc.internal.getFontSize() + letterSpacing;  // Adjust position based on letter width and spacing
    }
};