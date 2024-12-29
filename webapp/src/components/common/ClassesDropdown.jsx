import { useEffect, useState } from "react";
import { getClasses } from "../../services/classesService";


const ClassesDropdown = ({user, onError, onClassChange, classErrorMessage}) => {

    const [selectedClass, setSelectedClass] = useState({name : '', id : null});
    const [classes, setClasses] = useState([]);

    useEffect(() => {
        getClasses(user.role, user.accessToken)
            .then(response => setClasses(response.data))
            .catch(error => {
                console.log(error);
                onError(error)
            })
    }, [user]);


    const handleClassChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newClassName = selectedOption.value;
        const newClassID = selectedOption.getAttribute('data-key');
        const newClass = {name : newClassName, id : newClassID}
        setSelectedClass(newClass);
        onClassChange(newClass);
    }

    return(
        <div className='input-container'>
            <label className='label'>Sınıf Adı</label>
            <select
                className='input'
                onChange={handleClassChange}
                value={selectedClass.name}
            >
                <option value='' data-key={null}>Sınıf Seçiniz</option>
                {classes.map((singleClass) => (
                    <option
                        value={singleClass.name}
                        key={singleClass.id}
                        data-key={singleClass.id}
                    >
                        {singleClass.name}
                    </option>
                ))}
            </select>
            {classErrorMessage && <p className='error-message'>{classErrorMessage}</p>}
        </div>
    );
}
export default ClassesDropdown;