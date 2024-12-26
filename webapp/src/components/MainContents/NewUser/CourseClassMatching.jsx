import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../../contexts/AuthContext";
import { getAllCourses } from "../../../services/coursesService";





const CourseClassMatching = () => {
    const { user } = useContext(AuthContext);

    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState({name : '', id : null});
    const[classesOfSelectedCourse, setClassesOfSelectedCourse] = useState([]);

    useEffect(() => {
        const fetchCourses = async () => {
            try{
                const allCourses = await getAllCourses(user.accessToken);
                console.log(allCourses.data);
                setCourses(allCourses.data);
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
        const classes = courses.find(course => course.id === newCourseID).classEntityIds;
        console.log('classes of the course : ', classes);
        setClassesOfSelectedCourse(classes);
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
                        classesOfSelectedCourse.map((singleClass) => (
                            <div key={singleClass}>
                                <input type="checkbox" />
                                <span>{singleClass}</span>
                            </div>
                        ))
                    )}
                </>
            )}
        </>
    );
}
export default CourseClassMatching;