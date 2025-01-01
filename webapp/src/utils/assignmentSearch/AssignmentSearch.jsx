import { useContext, useEffect, useState } from "react";
import { getCoursesGivenBy } from "../../services/coursesService";
import { AuthContext } from "../../contexts/AuthContext";
import { getClassByID } from "../../services/classesService";
import DateInput from "../../components/common/DateInput";
import { getAssignments } from "../../services/assignmentService";




const AssignmentSearch = ({onSearchResults, onSearch, isFuture}) => {

    const { user } = useContext(AuthContext);

    const [courses, setCourses] = useState([]);
    const [searchCourse, setSearchCourse] = useState({name : '', id : null});

    const [classes, setClasses] = useState([]);
    const [searchClass, setSearchClass] = useState({name : '', id : null});

    const [searchDate, setSearchDate] = useState('');

    useEffect(() => {
        const fetchCourses = async () => {
            try{
                const response = await getCoursesGivenBy(user.role, user.accessToken, user.id);
                setCourses(response.data);    
            }catch(error){
                console.log(error);
            }
        }
        fetchCourses();
    }, []);

    const handleCourseChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        const newCourse = {name : newCourseName, id : newCourseID};
        setSearchCourse(newCourse);
        setSearchClass({name : '', id : null});
        loadClasses(newCourseID)
    }

    const loadClasses = async (courseID) => {
        try{
            const course = courses.find(course => course.id === courseID);
            console.log(course);
            const classIDs = user.role === 'ROLE_TEACHER'
                ? course.teacherCourses.find(c => c.teacherId === user.id).classIds
                : course.classEntityIds
            console.log(classIDs);
            const classPromises = classIDs.map(classID => getClassByID(classID, user.accessToken))
            const classResponses = await Promise.all(classPromises);
            const fetchedClasses = classResponses.map(response => response.data.data)
            console.log(fetchedClasses);
            setClasses(fetchedClasses);
        }catch(error){
            console.log(error);
        }
    }

    const handleClassChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newClassName = selectedOption.value;
        const newClassID = Number(selectedOption.getAttribute('data-key'));
        const newClass = {name : newClassName, id : newClassID};
        setSearchClass(newClass);
    }

    const handleSearch = async () => {
        try{
            onSearch(true);
            const filter = {
                classId : searchClass.name ? searchClass.id : null,
                courseId : searchCourse.name ? searchCourse.id : null,
                dueDate : searchDate
            }
            const response = await getAssignments(user.role, user.id, filter, user.accessToken);
            console.log(response);
            onSearchResults(filterAssignments(response.data))
        }catch(error){
            console.log(error);
        }finally{
            onSearch(false);
        }
    }

    const filterAssignments = (assignments) => {
        if(searchClass.name) {
            assignments = assignments.filter(assignment => assignment.className === searchClass.name);
        }
        if(searchCourse.name) {
            assignments = assignments.filter(assignment => assignment.courseName === searchCourse.name);
        }
        if(searchDate) {
            assignments = assignments.filter(assignment => new Date(assignment.dueDate) <= new Date(searchDate));
        }
        return assignments;
    }

    return(
        <div className="search">
            <div className="search-options">
                <div className="input-container">
                    <label className="label">Ders Adı</label>
                    <select
                        className="input"
                        value={searchCourse.name}
                        onChange={handleCourseChange}
                    >
                        <option value="" data-key={null} disabled>Ders Seçiniz</option>
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
                </div>

                <div className="input-container">
                    <label className="label">Sınıf Adı</label>
                    <select
                        className="input"
                        value={searchClass.name}
                        onChange={handleClassChange}
                        disabled={searchCourse.name === ''}
                    >
                        <option value="" data-key={null} disabled>Sınıf Seçiniz</option>
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
                </div>

                <DateInput
                    title="Bitiş Tarihi"
                    onInput={(date) => setSearchDate(date)}
                    isFutureInput={isFuture}
                />
            </div>
            <button className="btn" onClick={handleSearch}>Ara</button>
        </div>
    );
}
export default AssignmentSearch;