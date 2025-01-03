import { useContext, useEffect, useState } from "react";
import { ArrowDown, ArrowUp } from "../../../../../public/icons/Icons";
import Loading from "../../../common/Loading/Loading";
import { getAttendanceOfStudent, markAttendance, updateAttendance } from "../../../../services/attendanceService";
import { AuthContext } from "../../../../contexts/AuthContext";
import { areDatesEqual, get7DaysBefore, getPreviousDay, isToday } from "../../../../utils/dateUtils";
import Warning from "../../../common/IconComponents/Warning";

const ClassAttendance = ({ course, currentClass, attendanceDate }) => {
    const { user } = useContext(AuthContext);

    const [canEdit, setCanEdit] = useState(true);
    const [newAttendance, setNewAttendance] = useState(true);

    const [fetchError, setFetchError] = useState(false);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [saveError, setSaveError] = useState(false);

    const [isExpanded, setIsExpanded] = useState(false);
    const [loading, setLoading] = useState(false);
    const [students, setStudents] = useState([]);
    const [attendanceRecords, setAttendanceRecords] = useState({});
    const [attendanceError, setAttendanceError] = useState('');

    useEffect(() => {
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
                let isNew = true
                const studentAttendanceRecords = attendanceResponses.map((studentAttendance) => {
                    if (studentAttendance.data.length === 0)
                        return { comment: '', status: '', initial: false };
                    const targetDate = studentAttendance.data.find((attendance) =>
                        areDatesEqual(attendanceDate, attendance.date) && attendance.courseId == course.id
                    );
                    if(targetDate) {isNew = false, setNewAttendance(false);}
                    return targetDate
                        ? { comment: targetDate.comment, status: targetDate.status, initial: true, attendanceID : targetDate.attendanceId }
                        : { comment: '', status: '', initial: false };
                });
                const recordsObject = {};
                students.forEach((student, index) => {
                    recordsObject[student.id] = studentAttendanceRecords[index];
                });
                setAttendanceRecords(recordsObject);
                if(user.role === 'ROLE_TEACHER' && !isToday(attendanceDate) && !isNew) {setCanEdit(false);}
                else if(user.role === 'ROLE_COORDINATOR' && (new Date(attendanceDate) < new Date(get7DaysBefore())) && !isNew) {setCanEdit(false);}

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
        setAttendanceError('');

        let errorCount = 0;
        Object.entries(attendanceRecords).forEach(([id, record]) => {
            if(record.status === '') {errorCount++;}
        })

        if(errorCount > 0) {
            setAttendanceError('Her öğrencinin katılım bilgisi girilmelidir!');    
            return;
        }

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
                                        <th className="table-header">Öğrenci Adı</th>
                                        <th className="table-header">Yoklama Durumu</th>
                                        <th className="table-header">Açıklama</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {students.map((student) => {
                                        return(
                                            <tr className="table-row" key={student.id}>
                                                <td className="table-row-element">{student.name}</td>
                                                <td className="table-row-element">
                                                    <select
                                                        className="input"
                                                        value={attendanceRecords[student.id]?.status || ''}
                                                        onChange={(e) =>
                                                            handleStatusChange(student.id, e.target.value)
                                                        }
                                                        disabled={!canEdit}
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
                                                        disabled={!canEdit}
                                                    />
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                            {canEdit && <button className="save-btn btn" onClick={handleSubmit}>Kaydet</button>}
                        </>
                    )}
                    {saveSuccess && <p className='register-response' style={{color : 'green'}}>Yoklama başarıyla kaydedildi.</p>}
                    {saveError && <p className='register-response' style={{color : 'red'}}>Yoklama kaydedilemedi.</p>}
                    {attendanceError && <p className='register-response' style={{color : 'red'}}>{attendanceError}.</p>}
                </div>
            )}
        </div>
    );
};
export default ClassAttendance;