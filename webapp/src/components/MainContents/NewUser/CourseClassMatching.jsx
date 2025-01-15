import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getAllCourses } from "../../../services/coursesService";
import { getAllClasses } from "../../../services/classesService";

import React from 'react';
import Select from 'react-select';

const CourseClassMatching = ({onCourseSelection, onClassSelection, courseError = '', classError = ''}) => {
    const { user } = useContext(AuthContext);

    const [courses, setCourses] = useState([]);
    const [classes, setClasses] = useState([]);

    const [selectedCourse, setSelectedCourse] = useState({name : '', id : null});
    const [classesOfSelectedCourse, setClassesOfSelectedCourse] = useState([]);
    const [nonClassesOfSelectedCourse, setNonClassesOfSelectedCourse] = useState([]);

    const [selectedClassesOfCourse, setSelectedClassesOfCourse] = useState([]);
    const [selectedNonClassesOfCourse, setSelectedNonClassesOfCourse] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try{
                const allCourses = await getAllCourses(user.accessToken);
                setCourses(allCourses.data);
                const allClasses = await getAllClasses(user.accessToken);
                setClasses(allClasses.data);
            }catch(error){
                console.log(error)
            }
        }
        fetchData();
    }, []);

    const handleCourseChange =(event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        const newSelectedCourse = {name : newCourseName, id : newCourseID};
        setSelectedCourse(newSelectedCourse);
        if(newCourseName === '') {
            setSelectedClassesOfCourse([]);
            setSelectedNonClassesOfCourse([]);
            onCourseSelection({name : '', id : null});
            return;
        }

        const classListOfCourse = courses.find(course => course.id === newCourseID).classEntityIds;
        const classesOfCourse = classes
            .filter(singleClass => classListOfCourse.includes(singleClass.id))
            .map(singleClass => ({label : singleClass.name, value : Number(singleClass.id)}));
        setClassesOfSelectedCourse(classesOfCourse);

        const nonClassesOfCourse = classes
            .filter((singleClass) => !classListOfCourse.includes(singleClass.id))
            .map(singleClass => ({label : singleClass.name, value : singleClass.id}));
        setNonClassesOfSelectedCourse(nonClassesOfCourse);
        onCourseSelection(newSelectedCourse);
    }

    const handleClassChange = (selected, flag) => {
        flag 
            ? (setSelectedNonClassesOfCourse(selected), onClassSelection([...selectedClassesOfCourse, ...selected]))
            : (setSelectedClassesOfCourse(selected), onClassSelection([...selected, ...selectedNonClassesOfCourse]))
    }

    return(
        <>
            {courses.length !== 0 && (
                <>
                    <div className="input-fields">
                        <div className="input-container">
                            <select
                                className='input'
                                value={selectedCourse.name}
                                onChange={handleCourseChange}
                                >
                                <option value='' data-key={null}>Ders Seçiniz</option>
                                {courses.map((course) => (
                                    <option
                                    value={course.name}
                                    key={course.id}
                                    data-key={course.id}
                                    >
                                        {course.name}
                                    </option>
                                ))}
                            </select>
                            {courseError && <p className='error-message'>{courseError}</p>}
                        </div>
                    </div>

                    <div className="input-fields">
                        <div className="input-container">
                            <label className="label">Bu dersi alan sınıflar</label>
                            <Select
                                className="input"
                                isMulti
                                isDisabled={selectedCourse.name === ''}
                                placeholder={'Sınıfları seçiniz'}
                                options={classesOfSelectedCourse}
                                onChange={(selected) => {handleClassChange(selected, 0)}}
                                value={selectedClassesOfCourse}
                            />
                            {classError && <p className="error-message">{classError}</p>} 
                        </div>
                        <div className="input-container">
                            <label className="label">Bu dersi almayan sınıflar</label>
                            <Select
                                className="input"
                                isMulti
                                isDisabled={selectedCourse.name === ''}
                                placeholder={'Sınıfları seçiniz'}
                                options={nonClassesOfSelectedCourse}
                                onChange={(selected) => {handleClassChange(selected, 1)}}
                                value={selectedNonClassesOfCourse}
                            />
                        </div>
                    </div>
                </>
            )}
        </>
    );
}
export default CourseClassMatching;