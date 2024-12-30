import { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../../../contexts/AuthContext';
import { getClassByID } from '../../../../services/classesService';
import { getCoursesGivenBy } from '../../../../services/coursesService';

import ClassAttendance from './ClassAttendance';

import DateInput from '../../../common/DateInput';
import Loading from '../../../common/Loading/Loading';
import NoResult from '../../../common/IconComponents/NoResult';
import Warning from '../../../common/IconComponents/Warning';

const OfficerAttendance = () => {
    const [loading, setLoading] = useState(false);
    const [searching, setSearching] = useState(false);
    const [isSearched, setIsSearched] = useState(false);
    const [reloadRequest, setReloadRequest] = useState(false); 

    const { user } = useContext(AuthContext);

    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState({name : '', id : null});
    const [courseError, setCourseError] = useState('');
    const [classes, setClasses] = useState([]);
    const [attendanceDate, setAttendanceDate] = useState(new Date().toISOString().slice(0, 10));

    useEffect(() => {
        const fetchData = async () => {
            try{
                setLoading(true);
                const coursesData = await getCoursesGivenBy(user.role, user.accessToken, user.id);
                setCourses(coursesData.data);
            }catch(error){
                console.log(error);
                setReloadRequest(true);
            }finally{
                setLoading(false);
            }
        }
        fetchData();
    }, []);


    const handleSearch = async () => {
        if(!selectedCourse.name) {setCourseError('Lütfen sınıf seçiniz'); return;}
        try {
            setIsSearched(true);
            setSearching(true);
            const a = courses.find((course) => course.id === selectedCourse.id);
            console.log(a);
            console.log(a.classEntityIds);
            console.log(a.teacherCourses);
            // const classIDs = courses
            //     .find((course) => course.id === selectedCourse.id).teacherCourses
            //     .find((c) => c.courseId === selectedCourse.id)
            //     .classIds;
            const classIDs = user.role === 'ROLE_TEACHER'
                ? courses.find((course) => course.id === selectedCourse.id).teacherCourses.find((c) => c.teacherId === user.id).classIds
                : courses.find((course) => course.id === selectedCourse.id).classEntityIds;
                console.log(classIDs);
            const classPromises = classIDs.map((classID) =>
                getClassByID(classID, user.accessToken)
            );

            const classResponses = await Promise.all(classPromises);
            const fetchedClasses = classResponses.map((response) => response.data.data);
            setClasses(fetchedClasses);
        } catch (error) {
            console.error("Error fetching classes:", error);
            setReloadRequest(true);
        }finally {
            setSearching(false);
        }
    }

    const handleCourseChange = (event) => {
        setCourseError('');
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        const newSelectedCourse = { name: newCourseName, id: newCourseID };
        setSelectedCourse(newSelectedCourse);
    };

    if(loading) {return(<Loading/>);}
    if(reloadRequest) {return(<Warning/>)}
    return(
        <>
            <div className='search'>
                <div className='search-options'>
                    <div className="input-container">
                        <label className="label">Ders Adı</label>
                        <select
                            className='input'
                            value={selectedCourse.name}
                            onChange={handleCourseChange}
                        >
                            <option value="" data-key={null}>Ders Seçiniz</option>
                            {courses.length !== 0 && (
                                courses.map((course) => (
                                    <option
                                        value={course.name}
                                        key={course.id}
                                        data-key={course.id}
                                    >
                                        {course.name}
                                    </option>
                                ))
                            )}
                        </select>
                        {courseError && <p className='error-message'>{courseError}</p>}
                        {/* <p className='error-message' style={{visibility : courseError ? 'visible' : 'hidden'}}>{courseError}</p> */}
                    </div>
                    <DateInput
                        title='Yoklama Tarihi'
                        initialDate={attendanceDate}
                        onInput={(date) => setAttendanceDate(date)}
                    />
                    <button className='btn' onClick={handleSearch}>Ara</button>
                </div>
            </div>
            {isSearched && (
                searching ? (
                    <Loading/>
                ) : (
                    classes.length !== 0 ? (
                        classes.map((singleClass) => (
                            <ClassAttendance
                                key={singleClass.id}
                                course={selectedCourse}
                                currentClass={singleClass}
                                attendanceDate={attendanceDate}
                            />
                        ))) : (<NoResult/>)
                )
                
            )}
        </>
    );
}
export default OfficerAttendance;