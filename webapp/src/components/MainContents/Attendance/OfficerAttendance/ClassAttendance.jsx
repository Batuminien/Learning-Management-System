import { useEffect, useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import Loading from "../../../common/Loading/Loading";




const ClassAttendance = ({course, currentClass, attendanceDate}) => {

    const [isExpanded, setIsExpanded] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        console.log('the course is : ', course);
        console.log('the class is : ', currentClass);
        const studentsInfo = Object.entries(currentClass.studentIdAndNames).map(([id, name]) => ({
            id :Number(id),
            name : name
        }))
        console.log('students : ', studentsInfo);
    }, []);

    const handleExpand = () => {
        setIsExpanded(!isExpanded);
        console.log('fetch data');
    }

    return(
        <div className="unit-container">
            <div className="unit-header" onClick={() => handleExpand()}>
                <div className="unit-header-info">
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <div className="unit-subject">{currentClass.name}</div>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp/> : <ArrowDown/>}
                </button>
            </div>
            {isExpanded && (
                <div className="unit-body">
                    <div style={{border : '1px solid grey '}}></div>
                    {loading ? (
                        <Loading/>
                    ) : (
                        <p>will be displayed here</p>
                    )}
                </div>
            )}
        </div>
    );
}
export default ClassAttendance;