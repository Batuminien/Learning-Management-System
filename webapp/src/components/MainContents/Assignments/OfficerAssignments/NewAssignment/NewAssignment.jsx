import './NewAssignment.css';

import { useContext, useEffect, useState } from "react";
import { getCoursesGivenBy } from "../../../../../services/coursesService";
import { AuthContext } from "../../../../../contexts/AuthContext";

import Warning from "../../../../common/IconComponents/Warning";
import Loading from '../../../../common/Loading/Loading';
import { getClassByID } from "../../../../../services/classesService";
import DateInput from "../../../../common/DateInput";
import { calculateFileSize, isAllowedFileType } from "../../../../../utils/fileUtils";
import Document from "../../../../common/Document";
import { createAssignment, deleteAssignment, uploadDocument } from '../../../../../services/assignmentService';



const NewAssignment = () => {
    const { user } = useContext(AuthContext);

    const [allCourses, setAllCourses] = useState([]);
    const [assignmentCourse, setAssignmentCourse] = useState({name : '', id : null});
    const [courseError, setCourseError] = useState('');

    const [allClasses, setAllClasses] = useState([]);
    const [assignmentClass, setAssignmentClass] = useState({name : '', id : null});
    const [classError, setClassError] = useState('');

    const [assignmentDate, setAssignmentDate] = useState('');
    const [dateError, setDateError] = useState('');

    const [assignmentTitle, setAssignmentTitle] = useState('');
    const [titleError, setTitleError] = useState('');
    
    const [assignmentDescription, setAssignmentDescription] = useState('');

    const [assignmentDocument, setAssignmentDocument] = useState(null);
    const [fileError, setFileError] = useState('');

    const [reloadRequest, setReloadRequest] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [creationError, setCreationError] = useState(false);
    const [creationSuccess, setCreationSuccess] = useState(false);

    useEffect(() => {
        const fetchCourses = async () => {
            try{
                setIsLoading(true);
                const response = await getCoursesGivenBy(user.role, user.accessToken, user.id);
                console.log(response.data);
                setAllCourses(response.data);
            }catch(error){
                console.log(error);
                setReloadRequest(true);
            }finally{
                setIsLoading(false);
            }
        }
        fetchCourses();
    }, []);

    const handleCourseChange = (event) => {
        setCourseError('');
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        setAssignmentCourse({name : newCourseName, id : newCourseID});
        setAssignmentClass({ name : '', id : null});
        newCourseName ? loadClassesOf(newCourseID) : setAllClasses([]);
    }

    const loadClassesOf = async (courseID) => {
        try{
            const targetCourse = allCourses.find(course => course.id === courseID);
            console.log(targetCourse);
            const classPromises = targetCourse.classEntityIds.map((classID) => (
                getClassByID(classID, user.accessToken)
            ))
            const classResponses = await Promise.all(classPromises);
            const fetchedClasses = classResponses.map((response) => response.data.data);
            setAllClasses(fetchedClasses);

        }catch(error) {
            console.log(error);
            setReloadRequest(true);
        }
    }

    const handleClassChange = (event) => {
        setClassError('');
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newClassName = selectedOption.value;
        const newClassID = Number(selectedOption.getAttribute('data-key'));
        setAssignmentClass({name : newClassName, id : newClassID});
    }

    const handleTitleChange = (event) => {
        setTitleError('');
        const newTitle = event.target.value;
        setAssignmentTitle(newTitle);
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
        let hasError = false;
    
        if (!assignmentClass.name) {
            setClassError('Sınıf seçimi yapınız.');
            hasError = true;
        }
        if (!assignmentCourse.name) {
            setCourseError('Ders seçimi yapınız');
            hasError = true;
        }
        if (!assignmentDate) {
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
        if (!assignmentDate) {
            setDateError('Bitiş tarihi gelecekte olmalıdır.');
            hasError = true;
        }
    
        if (!hasError) {
            const payload = {
                teacherId: user.id,
                title: assignmentTitle,
                description: assignmentDescription,
                dueDate: assignmentDate,
                classId: assignmentClass.id,
                courseId: assignmentCourse.id,
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
                    setCreationSuccess(true);
            } catch (error) {
                console.error("Error during assignment creation:", error);
                setCreationError(true);
            }
        }
    };

    if(reloadRequest) {return(<Warning/>);}
    if(isLoading) {return(<Loading/>);}

    return(
        <>
            <div className="newAssignmentForm">
                <div className="input-container">
                    <label className="label">Ders Adı</label>
                    <select
                        className='input'
                        value={assignmentCourse.name}
                        onChange={handleCourseChange}
                    >
                        <option value='' data-key={null} disabled>Ders Seçiniz</option>
                        {allCourses.map((singleCourse) => (
                            <option
                                value={singleCourse.name}
                                key={singleCourse.id}
                                data-key={singleCourse.id}
                            >
                                {singleCourse.name}
                            </option>
                        ))}
                    </select>
                    {courseError && <p className='error-message'>{courseError}</p>}
                </div>

                <div className="input-container">
                    <label className="label">Sınıf Adı</label>
                    <select
                        className='input'
                        value={assignmentClass.name}
                        onChange={handleClassChange}
                        disabled={assignmentCourse.name === ''}
                    >
                        <option value='' data-key={null} disabled>Sınıf Seçiniz</option>
                        {allClasses && (
                            allClasses.map((singleClass) => (
                                <option
                                    value={singleClass.name}
                                    key={singleClass.id}
                                    data-key={singleClass.id}
                                >
                                    {singleClass.name}
                                </option>
                            ))
                        )}
                    </select>
                    {classError && <p className='error-message'>{classError}</p>}
                </div>
                
                <DateInput
                    title={'Bitiş Tarihi'}
                    onInput={(date) => setAssignmentDate(date)}
                    isFutureInput={true}
                    errorMessage={dateError}
                />
                
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
                    <textarea
                        className='input'
                        value={assignmentDescription}
                        onChange={(event) => setAssignmentDescription(event.target.value)}
                        style={{resize : 'none', width : '100%', aspectRatio : 5}}
                        lang='tr'
                        spellCheck='false'
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
        </>
    );
}
export default NewAssignment;