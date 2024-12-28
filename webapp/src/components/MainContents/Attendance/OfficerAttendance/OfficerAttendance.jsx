import { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../../../contexts/AuthContext';
import { getClassByID } from '../../../../services/classesService';

import DateInput from '../../../common/DateInput/DateInput';
import { getCoursesGivenBy } from '../../../../services/coursesService';

import Loading from '../../../common/Loading/Loading';
import NoResult from '../../../common/NoResult/NoResult';
import ClassAttendance from './ClassAttendance';
const OfficerAttendance = () => {
    const [loading, setLoading] = useState(false);
    const [isSearched, setIsSearched] = useState(false);    
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
                const courses = await getCoursesGivenBy(user.role, user.accessToken, user.id);
                setCourses(courses.data);

            }catch(error){
                console.log(error);
            }finally{
                setLoading(false);
            }
        }

        fetchData();
    }, []);


    const handleSearch = async () => {
        if(!selectedCourse.name) {setCourseError('Lütfen sınıf seçiniz'); return;}
        console.log(`get attendance record for ${selectedCourse.name} in ${attendanceDate}`);

        try {
            const classIDs = courses.find((course) => course.id === selectedCourse.id).classEntityIds;
            const classPromises = classIDs.map((classID) =>
                getClassByID(classID, user.accessToken)
            );

            const classResponses = await Promise.all(classPromises);
            const fetchedClasses = classResponses.map((response) => response.data.data);

            fetchedClasses.forEach((singleClass) => console.log(singleClass.name));
            setClasses(fetchedClasses);
        } catch (error) {
            console.error("Error fetching classes:", error);
        }

        setIsSearched(true);
    }

    const handleCourseChange = (event) => {
        setCourseError('');
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newCourseName = selectedOption.value;
        const newCourseID = Number(selectedOption.getAttribute('data-key'));
        const newSelectedCourse = { name: newCourseName, id: newCourseID };
        setSelectedCourse(newSelectedCourse);
        if(!newCourseName) {setClasses([]);}
    };

    if(loading) {return(<Loading/>);}
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
                classes.length !== 0 ? (
                    classes.map((singleClass) => (
                        <ClassAttendance 
                            course={selectedCourse}
                            currentClass={singleClass}
                            attendanceDate={attendanceDate}
                        />
                    ))
                ) : (
                    <NoResult/>
                )
            )}
        </>
    );
}
export default OfficerAttendance;