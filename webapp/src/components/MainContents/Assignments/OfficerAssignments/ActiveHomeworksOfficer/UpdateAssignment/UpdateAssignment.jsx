import { useContext, useState } from "react";

// import arrowDown from 'icons/arrow-down.svg';
// import arrowUp from 'icons/arrow-up.svg';

import { isDateInFuture } from "../../../../../../utils/dateUtils";
import { calculateFileSize, isAllowedFileType } from "../../../../../../utils/fileUtils";

import { deleteAssignment, deleteDocument, updateAssignment, uploadDocument } from "../../../../../../services/assignmentService";
import { AuthContext } from "../../../../../../contexts/AuthContext";
import Document from '../../../../../common/Document';

import { ArrowDown } from "../../../../../../../public/icons/Icons";
import { ArrowUp } from "../../../../../../../public/icons/Icons";

const UpdateAssignment = ({ assignment, onUpdate }) => {
    const { user } = useContext(AuthContext);

    const [isExpanded, setIsExpanded] = useState(false);

    const [newTitle, setNewTitle] = useState(assignment.title);
    const [titleError, setTitleError] = useState('');
    
    const [newDescription, setNewDescription] = useState(assignment.description);

    const [newDocument, setNewDocument] = useState(assignment.teacherDocuments);
    const [fileError, setFileError] = useState('');
    
    const [newDueDate, setNewDueDate] = useState(assignment.dueDate);
    const [dateError, setDateError] = useState('');

    const [updateSuccess, setUpdateSuccess] = useState(false);
    const [deletionSuccess, setDeletionSuccess] = useState(false);

    const handleDueDateChange = (event) => {
        const dateInput = event.target.value;
        setNewDueDate(dateInput);
    }

    const handleTitleChange = (event) => {
        const newTitle = event.target.value;
        setNewTitle(newTitle);
        setTitleError('');
    };

    const handleDescriptionChange = (event) => {
        const newDescription = event.target.value;
        setNewDescription(newDescription);
    }

    const handleFileChange = (event) => {
        const file = event.target.files[0];

        if(file) {

            if(!isAllowedFileType(file.type)) {
                setFileError("Dosya tipi desteklenmiyor. Yalnızca PDF, Word veya metin dosyaları yükleyebilirsiniz.");
                setNewDocument(null);
                return;
            }

            if(calculateFileSize(file) >= 5) {
                setFileError("Dosya boyutu 5 MB'den büyük olamaz.");
                setNewDocument(null);
                return;
            }

            setNewDocument(file);
            setFileError('');
        }
    };

    const handleUpdate = async () => {
        setUpdateSuccess(false);
        if(!newTitle || newTitle.length < 3) {
            setTitleError('Ödev başlığı üç karakterden fazla olmalıdır.');
            return;
        }

        if(!newDueDate || !isDateInFuture(newDueDate)) {
            setDateError('Lütfen geçerli bir tarih giriniz.');
            setNewDueDate(assignment.dueDate);
            return
        }

        //TODO : currently when the user updates the assignment even if there is no change on assignment's document it is deleted and uploaded again. it needs to be handled later on.
        const payload = {
            teacherId : user.id,
            title : newTitle,
            description : newDescription,
            dueDate : newDueDate,
            classId : assignment.classId,
            courseId : assignment.courseId,
            document : null
        }

        try {
            const response = await updateAssignment(assignment.id, payload, user.accessToken);
            console.log("update response : ",response);

            let documentUpdateResponse = null;

            if(assignment.teacherDocuments !== newDocument) {
                console.log(assignment.teacherDocuments);
                console.log(newDocument);
                if(newDocument === null) {
                    documentUpdateResponse = await deleteDocument(assignment.teacherDocuments.documentId, user.accessToken);
                    console.log("document deletion response : ",documentUpdateResponse);
                } else {
                    console.log('user wants to upload new file');
                    documentUpdateResponse = await uploadDocument(assignment.id, newDocument, user.accessToken);
                    console.log("document update response : ",documentUpdateResponse);
                    setNewDocument(documentUpdateResponse.data.data);
                }
            }
            setUpdateSuccess(true);
            onUpdate({
                ...assignment,
                title : newTitle,
                description : newDescription,
                dueDate : newDueDate,
                teacherDocuments : documentUpdateResponse !== null ? documentUpdateResponse.data.data : assignment.teacherDocuments
            });
        }catch(error) {
            console.log(error);
        }
    }

    const handleDeletion = () => {
        console.log('Assignment id : ',assignment.id);
        console.log('access token : ', user.accessToken);
        deleteAssignment(assignment.id, user.accessToken)
            .then(response => {
                console.log(response)
                setDeletionSuccess(true);
            })
            .catch(error => {
                console.log('fail : ',error);
            })
    }

    if(deletionSuccess) {
        return(
            <div className="unit-container">
                <p className="success-message" style={{fontSize : '16px'}}>Ödev başarıyla silindi.</p>
            </div>
        );
    }

    return(
        <div className="unit-container">
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info" style={{visibility : !isExpanded ? 'visible' : 'hidden'}}>
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <span className="unit-subject">{assignment.className}</span>
                    <span className="unit-subject">{assignment.courseName}</span>
                    <span className="assignment-title">{assignment.title}</span>
                    <span className="assignment-dueDate">{(new Date(assignment.createdDate)).toLocaleDateString("en-GB")} - {(new Date(assignment.dueDate)).toLocaleDateString("en-GB")}</span>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                </button>
            </div>
            {isExpanded &&
                <div className="assignment-info">
                    
                    <div className="input-container">
                        <label className="label">Sınıf Adı</label>
                        <input
                            type="text"
                            className="input"
                            disabled
                            value={assignment.className}
                        />
                    </div>

                    <div className="input-container">
                        <label className="label">Ders Adı</label>
                        <input
                            type="text"
                            className="input"
                            disabled
                            value={assignment.courseName}
                        />
                    </div>

                    <div className="input-container">
                        <label className="label">Bitiş Tarihi</label>
                        <input
                            className='input'
                            type='date'
                            value={newDueDate}
                            onChange={handleDueDateChange}
                        />
                        {dateError && <p className='error-message'>{dateError}</p>}
                    </div>
                    
                    <div className="input-container">
                        <label className="label">Başlık</label>
                        <input
                            className='input'
                            type='text'
                            value={newTitle}
                            onChange={handleTitleChange}
                        />
                        {titleError && <p className='error-message'>{titleError}</p>}
                    </div>

                    <div className="input-container">
                        <label className="label">Açıklama</label>
                        <input
                            className='input'
                            type='text'
                            value={newDescription}
                            onChange={handleDescriptionChange}
                        />
                    </div>

                    <div className="input-container">
                        <label className="label">Döküman</label>
                        {newDocument ? (
                            <Document 
                                file={newDocument}
                                isRemovable={true}
                                onRemove={() => setNewDocument(null)}
                            />
                        ) : (
                            <>
                            <input
                                className='input'
                                type='file'
                                value=''
                                accept=".pdf, .doc, .docx, .txt"
                                onChange={handleFileChange}
                                />
                            {fileError && <p className='error-message'>{fileError}</p>}
                            </>
                        )}
                    </div>
                    <div style={{display : 'flex', flexDirection : 'row', columnGap : '12px'}}>
                        <button
                            className='btn'
                            type='submit'
                            onClick={handleUpdate}
                        >
                            Güncelle
                        </button>
                        <button
                            className='btn delete-btn'
                            type='submit'
                            onClick={handleDeletion}
                        >
                            Sil
                        </button>
                    </div>
                    
                    {updateSuccess && <p className='success-message' style={{textAlign : 'center'}}>Ödev başarıyla güncelleştirildi.</p>}
                </div>   
            }
        </div>
    );
}
export default UpdateAssignment;