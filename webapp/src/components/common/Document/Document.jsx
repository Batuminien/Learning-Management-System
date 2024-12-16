import { useContext } from 'react';
import { downloadDocument } from '../../../services/assignmentService';
import deleteIcon from '/icons/delete.svg';
import { AuthContext } from '../../../contexts/AuthContext';

const Document = ({file, isRemovable = false, onRemove = null}) => {
    const { user } = useContext(AuthContext);

    const handleDocumentDownload = async () => {
        if(file.hasOwnProperty("documentId")) {
            try {
                const response = await downloadDocument(file.documentId, user.accessToken);
                const objectURL = URL.createObjectURL(response);
                const a = document.createElement('a');
                a.href = objectURL;
                a.download = file.fileName;
                a.style.display = 'none';
                document.body.appendChild(a);
                a.click();
                a.remove();
                URL.revokeObjectURL(objectURL);
            }catch(error) {
                console.log(error);
            }
        }
    }

    return(
        <div className="assignment-document-container">
            <span className="assignment-document" onClick={handleDocumentDownload}>{file?.name || file?.fileName}</span>
            {isRemovable &&
                <button type='button' className='delete-icon' onClick={() => onRemove()}>
                    <img src={deleteIcon} alt="remove file"/>
                </button>
            }
        </div>
    );
}
export default Document;