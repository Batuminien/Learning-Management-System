import { useContext, useEffect, useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import Loading from "../../../common/Loading/Loading";
import { getAttendanceOfStudent, markAttendance, updateAttendance } from "../../../../services/attendanceService";
import { AuthContext } from "../../../../contexts/AuthContext";
import { areDatesEqual, getPreviousDay } from "../../../../utils/dateUtils";
import Warning from "../../../common/IconComponents/Warning";

const ClassAttendance = ({ course, currentClass, attendanceDate }) => {
    const { user } = useContext(AuthContext);

    const [fetchError, setFetchError] = useState(false);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [saveError, setSaveError] = useState(false);

    const [isExpanded, setIsExpanded] = useState(false);
    const [loading, setLoading] = useState(false);
    const [students, setStudents] = useState([]);
    const [attendanceRecords, setAttendanceRecords] = useState({});

    useEffect(() => {
        // console.log(course);
        const studentsInfo = Object.entries(currentClass.studentIdAndNames).map(([id, name]) => ({
            id: Number(id),
            name: name,
        }));
        setStudents(studentsInfo);
    }, []);

    const handleExpand = async () => {
        const currentlyExpanded = isExpanded;
        setIsExpanded(!isExpanded);
        if(!currentlyExpanded) {
            try {
                setLoading(true);
                const params = {
                    startDate: getPreviousDay(attendanceDate),
                    endDate: attendanceDate,
                };
                const attendancePromises = students.map((student) =>
                    getAttendanceOfStudent(student.id, params, user.accessToken)
                );
                const attendanceResponses = await Promise.all(attendancePromises);
                const studentAttendanceRecords = attendanceResponses.map((studentAttendance) => {
                    console.log(studentAttendance);
                    if (studentAttendance.data.length === 0)
                        return { comment: '', status: '', initial: false };
                    const targetDate = studentAttendance.data.find((attendance) =>
                        areDatesEqual(attendanceDate, attendance.date) && attendance.courseId == course.id
                    );
                    console.log(targetDate);
                    return targetDate
                        ? { comment: targetDate.comment, status: targetDate.status, initial: true, attendanceID : targetDate.attendanceId }
                        : { comment: '', status: '', initial: false };
                });
                const recordsObject = {};
                students.forEach((student, index) => {
                    recordsObject[student.id] = studentAttendanceRecords[index];
                });
                setAttendanceRecords(recordsObject);
            } catch (error) {
                console.log(error);
                setFetchError(true);
            } finally {
                setLoading(false);
            }
        }
    };

    const handleStatusChange = (studentId, newStatus) => {
        setAttendanceRecords((prevRecords) => ({
            ...prevRecords,
            [studentId]: {
                ...prevRecords[studentId],
                status: newStatus,
            },
        }));
    };

    const handleCommentChange = (studentId, newComment) => {
        setAttendanceRecords((prevRecords) => ({
            ...prevRecords,
            [studentId]: {
                ...prevRecords[studentId],
                comment: newComment,
            },
        }));
    };

    const handleSubmit = async () => {
        setSaveError(false);
        setSaveSuccess(false);
        try {
            const attendancePromises = Object.entries(attendanceRecords).map(async ([studentId, record]) => {
                const attendanceData = {
                    studentId: Number(studentId),
                    date: attendanceDate,
                    status: record.status,
                    comment: record.comment || "",
                    classId: currentClass.id,
                    courseId: course.id,
                };
                return record.initial
                    ? updateAttendance(record.attendanceID, attendanceData, user.accessToken)
                    : markAttendance(attendanceData, user.accessToken)
    
                
            });
    
            const responses = await Promise.all(attendancePromises);
            console.log("Attendance submission successful:", responses);
            setSaveSuccess(true);
        } catch (error) {
            console.log(error);
            setSaveError(true);
        }
    };
    

    return (
        <div className="unit-container">
            <div className="unit-header" onClick={() => handleExpand()}>
                <div className="unit-header-info">
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <div className="unit-subject">{currentClass.name}</div>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp /> : <ArrowDown />}
                </button>
            </div>
            {isExpanded && (
                <div className="unit-body">
                    <div style={{ border: '1px solid grey ' }}></div>
                    {loading ? (
                        <Loading />
                    ) : fetchError 
                        ? (<Warning/>)
                        : (
                        <>
                            <table className="attendance-table">
                                <thead className="table-header">
                                    <tr>
                                        <th className="table-header"></th>
                                        <th className="table-header">Öğrenci Adı</th>
                                        <th className="table-header">Yoklama Durumu</th>
                                        <th className="table-header">Açıklama</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {students.map((student, index) => (
                                        <tr className="table-row" key={student.id}>
                                            <td className="table-row-element">{index}</td>
                                            <td className="table-row-element">{student.name}</td>
                                            <td className="table-row-element">
                                                <select
                                                    className="input"
                                                    value={attendanceRecords[student.id]?.status || ''}
                                                    onChange={(e) =>
                                                        handleStatusChange(student.id, e.target.value)
                                                    }
                                                >
                                                    <option value=""></option>
                                                    <option value="PRESENT">Katıldı</option>
                                                    <option value="ABSENT">Katılmadı</option>
                                                    <option value="EXCUSED">Mazeretli</option>
                                                    <option value="LATE">Geç geldi</option>
                                                </select>
                                            </td>
                                            <td className="table-row-element">
                                                <input
                                                    type="text"
                                                    className="input"
                                                    value={attendanceRecords[student.id]?.comment || ''}
                                                    onChange={(e) =>
                                                        handleCommentChange(student.id, e.target.value)
                                                    }
                                                />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            <button className="save-btn btn" onClick={handleSubmit}>Kaydet</button>
                        </>
                    )}
                    {saveSuccess && <p className='register-response' style={{color : 'green'}}>Yoklama başarıyla kaydedildi.</p>}
                    {saveError && <p className='register-response' style={{color : 'red'}}>Yoklama kaydedilemedi.</p>}
                </div>
            )}
        </div>
    );
};
export default ClassAttendance;