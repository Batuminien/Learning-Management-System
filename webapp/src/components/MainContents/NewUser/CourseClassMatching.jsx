import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getAllCourses } from "../../../services/coursesService";
import { getAllClasses } from "../../../services/classesService";

const CourseClassMatching = () => {
    const { user } = useContext(AuthContext);

    const [courses, setCourses] = useState([]);
    const [classes, setClasses] = useState([]);

    const [selectedCourse, setSelectedCourse] = useState({name : '', id : null});
    const[classesOfSelectedCourse, setClassesOfSelectedCourse] = useState([]);
    const [nonClassesOfSelectedCourse, setNonClassesOfSelectedCourse] = useState('');

    useEffect(() => {
        const fetchCourses = async () => {
            try{
                const allCourses = await getAllCourses(user.accessToken);
                setCourses(allCourses.data);
                const allClasses = await getAllClasses(user.accessToken);
                setClasses(allClasses.data);
            }catch(error){
                console.log(error)
            }
        }
        fetchCourses();
    }, []);

    const handleCourseChange =(event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        const newSelectedCourse = {name : newCourseName, id : newCourseID};
        setSelectedCourse(newSelectedCourse);
        const classListOfCourse = courses.find(course => course.id === newCourseID).classEntityIds;
        const classesOfCourse = classes.filter((singleClass) => classListOfCourse.some(c => c === singleClass.id));
        setClassesOfSelectedCourse(classesOfCourse);
        const nonClassesOfCourse = classes.filter((singleClass) => !classListOfCourse.some(c => c === singleClass.id));
        setNonClassesOfSelectedCourse(nonClassesOfCourse);
    }

    return(
        <>
            {courses.length !== 0 && (
                <>
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

                    {classesOfSelectedCourse.length !== 0 && (
                        <>
                        <p>bu dersi alan sınıflar</p>
                        {classesOfSelectedCourse.map((singleClass) => (
                            <span key={singleClass.id}>
                                <input type="checkbox" />
                                <span>{singleClass.name}</span>
                            </span>
                        ))}
                        <p>bu dersi almayan sınıflar</p>
                        {nonClassesOfSelectedCourse.map((singleClass) => (
                            <span key={singleClass.id}>
                                <input type="checkbox" />
                                <span>{singleClass.name}</span>
                            </span>
                        ))}
                        </>
                    )}
                </>
            )}
        </>
    );
}
export default CourseClassMatching;