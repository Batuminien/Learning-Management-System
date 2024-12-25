import { useEffect, useState } from "react";
import { getAllSubjectsOf } from "../../../services/coursesService";






const SubjectsDropdown = ({user, classID, onError, subjectErrorMessage, onSubjectChange}) => {



    const [selectedSubject, setSelectedSubject] = useState({name : '', id : null});
    const [subjects, setSubjects] = useState([]);

    useEffect(() => {
        if(classID){
            getAllSubjectsOf(classID, user.accessToken)
                .then(response => setSubjects(response.data))
                .catch(error => onError(error))
        }
    }, [classID]);


    const handleSubjectChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newSubjectName = selectedOption.value;
        const newSubjectID = selectedOption.getAttribute('data-key');
        const newSubject = {name : newSubjectName, id : newSubjectID}
        setSelectedSubject(newSubject);
        onSubjectChange(newSubject);
    }

    return(
        <div className='input-container'>
            <label className='label'>Ders Adı</label>
            <select
                className='input'
                value={selectedSubject.name}
                onChange={handleSubjectChange}
            >
                <option value='' data-key={null}>Ders Seçiniz</option>
                {subjects.map((singleSubject) => (
                    <option
                        value={singleSubject.name}
                        key={singleSubject.id}
                        data-key={singleSubject.id}
                    >
                        {singleSubject.name}
                    </option>
                ))}
            </select>
            {subjectErrorMessage && <p className='error-message'>{subjectErrorMessage}</p>}
        </div>    
    );
}
export default SubjectsDropdown;