import { useContext, useEffect, useState } from "react";
import { getAllClasses } from "../../../services/classesService";
import { AuthContext } from "../../../contexts/AuthContext";
import { getAllCourses, getAllSubjectsOf } from "../../../services/coursesService";




const ClassCourseMatching = () => {
    const { user } = useContext(AuthContext);

    const [addNewClass, setAddNewClass] = useState(false);
    
    const [allClasses, setAllClasses] = useState([]);
    const [selectedClass, setSelectedClass] = useState({name : '', id : null});

    const [coursesOfAllClasses, setCoursesOfAllClasses] = useState([]);
    const [coursesOfSelectedClass, setCoursesOfSelectedClass] = useState([]);

    const handleNewClass = () => {
        // setClasses((prevClasses) => [...prevClasses, 'yeni ders']);
        console.log('adding new class');
        setAddNewClass(false);
    }



    const handleClassChange = (event) => {
        const selectedOption = event.target.options[event.target.selectedIndex];
        const newClassName = selectedOption.value;
        const newClassID = selectedOption.getAttribute('data-key');
        const newSelectedClass = {name : newClassName, id : newClassID, index : event.target.selectedIndex - 1};
        console.log(newSelectedClass)
        setSelectedClass(newSelectedClass);
        setCoursesOfSelectedClass(coursesOfAllClasses[newSelectedClass.index]);
    }

    useEffect(() => {
        const fetchData = async () => {
            try {
                const classes = (await getAllClasses(user.accessToken)).data;
                const aaa = await getAllCourses(user.accessToken);
                console.log(aaa);
                setAllClasses(classes);

                const classCourses = [];
                
                await Promise.all(
                    classes.map(async (singleClass) => {
                        const allCoursesOfClass = await getAllSubjectsOf(singleClass.id, user.accessToken);
                        classCourses.push(allCoursesOfClass.data);
                    })
                );
                setCoursesOfAllClasses(classCourses);
                console.log(classCourses);
            }catch(error) {
                console.log(error);
            }
        }

        fetchData();

    }, []);


    return(
        <>
            {/* {classes.map((singleClass) => (
                <p>{singleClass}</p>
            ))} */}
            {addNewClass && (
                <>
                    <select 
                        className="input"
                        value={selectedClass}
                        onChange={handleClassChange}
                    >
                        <option value='' data-key={null}>Sınıf Seçiniz</option>
                        {allClasses.map((singleClass) => (
                            <option
                                value={singleClass.name}
                                key={singleClass.id}
                                data-key={singleClass.id}
                            >
                                {singleClass.name}
                            </option>
                        ))}
                    </select>
                    
                    <select
                        className='input'
                    >
                        <option value='' data-key={null}>Ders Seçiniz</option>
                        {coursesOfSelectedClass && (
                            coursesOfSelectedClass.map((singleCourse) => (
                                <option 
                                    value={singleCourse.name}
                                    data-key={singleCourse.id}
                                >
                                    {singleCourse.name}
                                </option>
                            )
                        ))}
                    </select>
              
                    <p>ders seç</p>
                    <button onClick={handleNewClass}>ekle</button>
                </>
            )}
            
            <button onClick={() => {setAddNewClass(true)}}>Ders Ekle</button>
        </>
    );
}
export default ClassCourseMatching;