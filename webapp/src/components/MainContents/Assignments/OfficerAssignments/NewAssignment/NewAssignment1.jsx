import './NewAssignment.css';

import { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../../../../contexts/AuthContext';

import { getClasses } from '../../../../../services/classesService';
import { getAllSubjectsOf, getCoursesGivenBy } from '../../../../../services/coursesService';
import { createAssignment, deleteAssignment, uploadDocument } from '../../../../../services/assignmentService';

import { isDateInFuture } from '../../../../../utils/dateUtils';
import { calculateFileSize, isAllowedFileType } from '../../../../../utils/fileUtils';
import Document from '../../../../common/Document';
import Warning from '../../../../common/IconComponents/Warning';


const NewAssignment = () => {
    const { user } = useContext(AuthContext);

    const [allClasses, setAllClasses] = useState([]);
    const [allSubjectsOfClass, setAllSubjectsOfClass] = useState([]);

    const [assignmentClass, setAssignmentClass] = useState({name : '', id : null});
    const [classError, setClassError] = useState('');

    const [assignmentSubject, setAssignmentSubject] = useState({name : '', id : null});
    const [subjectError, setSubjectError] = useState('');

    const [assignmentDueDate, setAssignmnentDueDate] = useState('');
    const [dateError, setDateError] = useState('');

    const [assignmentTitle, setAssignmentTitle] = useState('');
    const [titleError, setTitleError] = useState('');

    const [assignmentDescription, setAssignmentDescription] = useState('');

    const [assignmentDocument, setAssignmentDocument] = useState('');
    const [fileError, setFileError] = useState('');

    const [creationError, setCreationError] = useState(false);
    const [creationSuccess, setCreationSuccess] = useState(false);

    const [fetchError, setFecthError] = useState(false);
    const [uploadError, setUploadError] = useState(false);

    useEffect(() => {
        getClasses(user.role, user.accessToken)
            .then(response => {
                setAllClasses(response.data);
            })
            .catch(error => {
                console.log(error);
                setFecthError(true);
            })
        const fetchCourses = async () => {
            const response = await getCoursesGivenBy(user.role, user.accessToken, user.id);
            console.log(response.data);
        }
        fetchCourses();
    }, [user.accessToken]);

    const clearMessages = () => {
        setCreationError(false);
        setCreationSuccess(false);
    }

    const loadCourses = async (classID) => {
        getAllSubjectsOf(classID, user.accessToken)
            .then(data => {
                setAllSubjectsOfClass(data.data);
                setAssignmentSubject({name : '', id : null});
            })
            .catch(error => {
                console.error(error);
                setFecthError(true);
            });
    };

    const handleClassChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newClassName = selectedOption.value;
        const newClassID = selectedOption.getAttribute('data-key');
        setAssignmentClass({name : newClassName, id : newClassID});
        loadCourses(newClassID);
        setClassError('');
        clearMessages();
    };

    const handleSubjectChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex]
        const newSubjectName = selectedOption.value;
        const newSubjectID = selectedOption.getAttribute('data-key');
        setAssignmentSubject({name : newSubjectName, id : newSubjectID});
        setSubjectError('');
        clearMessages();
    };

    const handleDueDateChange = (event) => {
        const dateInput = event.target.value;
        setDateError('');
        setAssignmnentDueDate(dateInput);
    };

    const handleTitleChange = (event) => {
        const newTitle = event.target.value;
        setAssignmentTitle(newTitle);
        setTitleError('');
        clearMessages();
    };

    const handleDescriptionChange = (event) => {
        const newDescription = event.target.value;
        setAssignmentDescription(newDescription);
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];

        if(file) {

            if(!isAllowedFileType(file.type)) {
                setFileError("Dosya tipi desteklenmiyor. Yalnızca PDF, Word veya metin dosyaları yükleyebilirsiniz.");
                setAssignmentDocument(null);
                return;
            }

            if(calculateFileSize(file) >= 5) {
                setFileError("Dosya boyutu 5 MB'den büyük olamaz.");
                setAssignmentDocument(null);
                return;
            }
            setAssignmentDocument(file);
            setFileError('');
        }
    };

    const handleSubmit = async () => {
        resetErrorMessages();
        let hasError = false;
    
        if (!assignmentClass.name) {
            setClassError('Sınıf seçimi yapınız.');
            hasError = true;
        }
        if (!assignmentSubject.name) {
            setSubjectError('Ders seçimi yapınız');
            hasError = true;
        }
        if (!assignmentDueDate) {
            setDateError('Bitiş tarihi seçiniz.');
            hasError = true;
        }
        if (!assignmentTitle) {
            setTitleError('Ödev Başlığı giriniz.');
            hasError = true;
        }
        if (assignmentTitle.trim().length < 3) {
            setTitleError('Ödev başlığı 3 karakterden fazla olmalıdır.');
            hasError = true;
        }
        if (!assignmentDueDate || !isDateInFuture(assignmentDueDate)) {
            setDateError('Bitiş tarihi gelecekte olmalıdır.');
            hasError = true;
        }
    
        if (!hasError) {
            const payload = {
                teacherId: user.id,
                title: assignmentTitle,
                description: assignmentDescription,
                dueDate: assignmentDueDate,
                classId: assignmentClass.id,
                courseId: assignmentSubject.id,
                document: null,
            };
    
            try {
                const response = await createAssignment(payload, user.accessToken);
                if (response.success && assignmentDocument) {
                        const assignmentID = response.data.id;
                        try {
                            const uploadResponse = await uploadDocument(
                                assignmentID,
                                assignmentDocument,
                                user.accessToken
                            );
                            console.log('upload document to assignment response', uploadResponse);
                        } catch (uploadError) {
                            console.error(
                                "Failed to upload the document, need to delete the assignment",
                                assignmentID
                            );
                            
                            try {
                                await deleteAssignment(assignmentID, user.accessToken);
                                console.log("Assignment deleted successfully:", assignmentID);
                            } catch (deletionError) {
                                console.error("Failed to delete the assignment:", assignmentID, deletionError);
                            }
    
                            throw uploadError;
                        }
                    }
                    resetForm();
                    setCreationSuccess(true);
            } catch (error) {
                console.error("Error during assignment creation:", error);
                setCreationError(true);
            }
        }
    };
    

    const resetForm = () => {
        setAssignmentClass({name : '', id : null});
        setAssignmentSubject({name : '', id : null});
        setAssignmnentDueDate('');
        setAssignmentTitle('');
        setAssignmentDescription('');
        setAssignmentDocument(null);
    }

    const resetErrorMessages = () => {
        setClassError('');
        setSubjectError('');
        setDateError('');
        setTitleError('');
        setFileError('');
        setCreationError('');
        setCreationSuccess('');
    }

    if(fetchError) {return(<Warning/>);}

    return (
    <>
        <div className="newAssignmentForm">
            <div className="input-container">
                <label className="label">Sınıf Adı</label>
                <select
                    className="input"
                    value={assignmentClass.name}
                    onChange={handleClassChange}
                >
                    <option value="" data-key={null} disabled>Sınıf Seçiniz</option>
                    {allClasses.map((singleClass) => (
                        <option value={singleClass.name} key={singleClass.id} data-key={singleClass.id}>
                            {singleClass.name}
                        </option>
                    ))}
                </select>
                {classError && <p className='error-message'>{classError}</p>}
            </div>

            <div className="input-container">
                <label className="label">Ders Adı</label>
                <select
                    className='input'
                    value={assignmentSubject.name}
                    onChange={handleSubjectChange}
                    disabled={assignmentClass.name === ''}
                >
                    <option value="" disabled>Ders Seçiniz</option>
                    {allSubjectsOfClass &&
                        allSubjectsOfClass.map((singleSubject) => (
                            <option value={singleSubject.name} key={singleSubject.id} data-key={singleSubject.id}>
                                {singleSubject.name}
                            </option>
                    ))}
                </select>
                {subjectError && <p className='error-message'>{subjectError}</p>}
            </div>

            <div className="input-container">
                <label className="label">Bitiş Tarihi</label>
                <input
                    className='input'
                    type='date'
                    value={assignmentDueDate}
                    onChange={handleDueDateChange}
                />
                {dateError && <p className='error-message'>{dateError}</p>}
            </div>

            <div className="input-container">
                <label className="label">Başlık</label>
                <input
                    className='input'
                    type='text'
                    value={assignmentTitle}
                    onChange={handleTitleChange}
                />
                {titleError && <p className='error-message'>{titleError}</p>}
            </div>

            <div className="input-container">
                <label className="label">Açıklama</label>
                <input
                    className='input'
                    type='text'
                    value={assignmentDescription}
                    onChange={handleDescriptionChange}
                />
            </div>

            {assignmentDocument ? (
                <Document
                    file={assignmentDocument}
                    isRemovable={true}
                    onRemove={() => setAssignmentDocument(null)}
                />
            ) : (
                <div className="input-container">
                    <label className="label">Döküman</label>
                    <input
                        className='input'
                        type='file'
                        accept=".pdf, .doc, .docx, .txt"
                        onChange={handleFileChange}
                        value=''
                    />
                    {fileError && <p className='error-message'>{fileError}</p>}
                </div>
            )}

            <button
                type='submit'
                className="btn"
                onClick={handleSubmit}
            >
                Oluştur
            </button>
            {creationError && 
                <p className='error-message' style={{ whiteSpace : 'pre-line'}}>
                    Ödev oluşturulukren hata! <br />
                    Lütfen sayfayı yenileyerek tekrar deneyiniz. <br />
                </p>
            }
            {creationSuccess && <p className='success-message'>Ödev başarıyla oluşturuldu.</p>}
        </div>
        {fetchError && 
            <p className='error-message'>
                Sayfa yüklenirken hata oluştu! <br/>
                Lütfen sayfayı yenileyiniz. <br />
            </p>
        }
        {uploadError &&
            <p className='error-message'>
                Döküman eklenemedi!
            </p>
        }
    </>
    );
};
export default NewAssignment;