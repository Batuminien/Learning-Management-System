import { AuthContext } from '../../../../../contexts/AuthContext';
import { getClassByID, getClasses } from '../../../../../services/classesService';
import './GradingAssignment.css';
import { useContext, useEffect, useState, useRef } from "react";
import { bulkGradeAssignment } from '../../../../../services/assignmentService';
import Document from '../../../../common/Document';

import { ArrowUp } from '../../../../../../public/icons/Icons';
import { ArrowDown } from '../../../../../../public/icons/Icons';

const GradeAssignment = ({ assignment, onUpdate }) => {
    const { user } = useContext(AuthContext);
    const [isExpanded, setIsExpanded] = useState(false);
    const [students, setStudents] = useState([]);
    const inputsRef = useRef({});
    const [errorStudents, setErrorStudents] = useState([]);
    const [gradingSuccess, setGradingSuccess] = useState('');
    const [gradingFailure, setGradingFailure] = useState('');

    useEffect(() => {
        if (isExpanded) {
            const fetchStudents = async () => {
                try {
                    const response = await getClasses(user.role, user.accessToken);
                    const assignmentClass = response.data.find(singleClass => singleClass.id === assignment.classId);
                    const studentsInfo = Object.entries(assignmentClass.studentIdAndNames).map(([id, name]) => ({
                        id : Number(id),
                        name : name
                    }))
                    setStudents(studentsInfo);
                } catch (error) {
                    console.log(error);
                }
            };
            fetchStudents();
        }
    }, [isExpanded]);

    const handleSubmission = async () => {
        setGradingFailure('');
        setGradingSuccess('');
        const results = [];
        const errors = [];

        students.forEach((student) => {
            const gradeInput = inputsRef.current[student.id]?.grade;
            const feedbackInput = inputsRef.current[student.id]?.feedback;
            const grade = gradeInput?.value.trim();

            if (!grade) {
                errors.push(student.id);
            } else {
                results.push({
                    studentId : Number(student.id),
                    grade : {
                        grade : Number(grade),
                        feedback: feedbackInput?.value.trim() || '',
                    }
                });
            }
        });

        setErrorStudents(errors);
        if (errors.length > 0) {return;}

        try {
            const response = await bulkGradeAssignment(assignment.id, results, user.accessToken);
            console.log(response);
            if(response.success)
                setGradingSuccess('Ödev başarıyla notlandırıldı.');
            else
                throw response.message;
        }catch(error) {
            console.log(error);
            setGradingFailure('Ödev notlandırılması sırasında hata!');
        }

        onUpdate(results);
    };

    return (
        <div className="unit-container">
            <div className="unit-header" onClick={() => setIsExpanded((prev) => !prev)}>
                <div className="unit-header-info">
                    <img src="https://placeholder.pics/svg/32x32" alt="icon" />
                    <span className="unit-subject">{assignment.courseName}</span>
                    <span className="assignment-title">{assignment.title}</span>
                    <span className="assignment-dueDate">
                        {(new Date(assignment.createdDate)).toLocaleDateString("en-GB")} - {(new Date(assignment.dueDate)).toLocaleDateString("en-GB")}
                    </span>
                </div>
                <button className="expand-btn">
                    {isExpanded ? <ArrowUp /> : <ArrowDown />}
                </button>
            </div>

            {isExpanded && (
                <div className="unit-body">
                    {students.map((student) => {
                        const studentSubmission = assignment.studentSubmissions.find((submission) => submission.studentId === student.id)
                        inputsRef.current[student.id] = inputsRef.current[student.id] || {};
                        return(
                            <div className="unit-container">
                                <div className="grading-body">
                                    <div className="unit-body-section">
                                        <div className="unit-section-title">Öğrenci Adı</div>
                                        <div className="unit-section-text">{student.name}</div>
                                    </div>
                                    {studentSubmission
                                        ? (
                                            <>
                                                <div className="unit-body-section">
                                                    <div className="unit-section-title">Yüklenen Döküman</div>
                                                    {studentSubmission.document
                                                        ? <Document file={studentSubmission.document} isRemovable={false}/>
                                                        : <i>Döküman eklenmedi.</i>
                                                    }
                                                </div>
                                                <div className="unit-body-section">
                                                    <div className="unit-section-title">Teslim notu</div>
                                                    {studentSubmission.comment
                                                        ? <div className="unit-section-text">{studentSubmission.comment}</div> 
                                                        : <i>Teslim notu eklenmedi.</i>
                                                    }
                                                </div>
                                            </>

                                        )
                                        : <i style={{gridColumn : 'span 2'}}>Öğrenci ödevi teslim etmedi.</i>
                                    }
                                    <div className="unit-body-section">
                                        <div className="unit-section-title">Ödev Notu</div>
                                        <input
                                            ref={(el) => (inputsRef.current[student.id].grade = el)}
                                            className={`input grade-input ${
                                                errorStudents.includes(student.id) ? "error-input" : ""
                                            }`}
                                            type='number'
                                            min='0'
                                            max='100'
                                            placeholder='0 - 100'
                                            defaultValue={studentSubmission ? studentSubmission.grade : ''}
                                        />
                                    </div>

                                    <div className="unit-body-section">
                                        <div className="unit-section-title">Geri Dönüş</div>
                                        <input
                                            ref={(el) => (inputsRef.current[student.id].feedback = el)}
                                            type='text'
                                            className='input student-feedback'
                                            placeholder='Enter feedback'
                                            defaultValue={studentSubmission ? studentSubmission.feedback : ''}
                                        />
                                    </div>
                                </div>
                            </div>
                        )
                    })}
                    {errorStudents.length > 0 && (
                        <div className="error-message">
                            Lütfen her öğrenci için not giriniz.
                        </div>
                    )}
                    <button
                        type="submit"
                        className="btn"
                        onClick={handleSubmission}
                    >
                        Tamamla
                    </button>
                    {gradingSuccess && <p className='success-message'>{gradingSuccess}</p>}
                    {gradingFailure && <p className='error-message'>{gradingFailure}</p>}
                </div>
            )}
        </div>
    );
};

export default GradeAssignment;
 