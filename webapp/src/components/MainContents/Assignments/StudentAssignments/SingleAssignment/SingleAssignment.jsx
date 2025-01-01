import { useContext, useState } from 'react';

import './SingleAssignment.css';
import { submitAssignment, unsubmitStudentAssignment } from '../../../../../services/assignmentService';
import { AuthContext } from '../../../../../contexts/AuthContext';
import Document from '../../../../common/Document';
import { ArrowDown, ArrowUp } from '../../../../../../public/icons/Icons';

const SingleAssigment = ({ assignment, refreshAssignments, status }) => {
    const { user } = useContext(AuthContext);

    const [isExpanded, setIsExpanded] = useState(false);
    const [submitError, setSubmitError] = useState(false);
    const [uploadedFile, setUploadedFile] = useState(null);
    const [submissionComment, setSubmissionComment] = useState('');

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        setUploadedFile(file);
    }

    const handleAssignmentSubmit = async () => {
        setSubmitError(false);
        const formData = new FormData();
        if(uploadedFile){
            formData.append('document', uploadedFile);
        }
        formData.append('submissionComment', submissionComment);

        try {
            const response = await submitAssignment(assignment.id, formData, user.accessToken);
            console.log(response);
            refreshAssignments();
        }catch(error) {
            console.log(error);
            setSubmitError(true);
        }
    }
    
    const handleAssignmentUnsubmit = async () => {
        try{
            const response = await unsubmitStudentAssignment(assignment.id, user.accessToken);
            console.log(response);
            refreshAssignments();
        }catch(error){
            console.log(error);
        }
    }

    return(
        <div className='unit-container'>
            <div className='unit-header' onClick={() => setIsExpanded((prev) => !prev)}>
                <div className='unit-header-info'>
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className='unit-subject'>{assignment.courseName}</span>
                    <span className='unit-title'>{assignment.title}</span>
                    <span className='unit-dueDate'>{(new Date(assignment.createdDate)).toLocaleDateString('en-GB')} - {(new Date(assignment.dueDate)).toLocaleDateString('en-GB')}</span>
                </div>
                <button className='expand-btn'>
                    {isExpanded ? <ArrowUp /> : <ArrowDown />}
                </button>
            </div>
            {isExpanded && (
                <div className='unit-body'>
                    <div style={{border : '1px solid grey'}}></div>

                    <div className='unit-body-section'>
                        <label className='unit-section-title'>Açıklama</label>
                        {assignment.description ? (
                            <p className='unit-section-text'>{assignment.description}</p>
                        ) : (
                            <i className='unit-section-text'>Açıklama yok.</i>
                        )}
                    </div>

                    <div className='unit-body-section'>
                        <label className='unit-section-title'>Yardımcı materyaller</label>
                        {assignment.teacherDocument ? (
                            <Document 
                                file={assignment.teacherDocument}
                            />
                        ) : (
                            <i className='unit-section-text'>Döküman eklenmedi.</i>
                        )}
                    </div>
                    
                    {status === 'PENDING' && (
                        <>
                            <div className='unit-body-section'>
                                {uploadedFile ? (
                                    <>
                                        <label className='unit-section-title'>Yüklenen döküman</label>
                                        <Document 
                                            file={uploadedFile}
                                            isRemovable={true}
                                            onRemove={() => setUploadedFile(null)}
                                        />
                                    </>
                                ) : (
                                    <>
                                        <label className='unit-section-title'>Döküman ekle</label>
                                        <input type='file' onChange={handleFileUpload}/>
                                    </>
                                )}
                            </div>
                            <div className='unit-body-section'>
                                <label className='unit-section-title'>Teslim notu</label>
                                <div className='input-container'>
                                    
                                <textarea
                                    className='input'
                                    type='textarea'
                                        
                                    value={submissionComment}
                                    onChange={(event) => setSubmissionComment(event.target.value)}
                                    lang='tr'
                                    spellCheck='false'
                                    />
                                    </div>
                            </div>
                            <button className='btn' onClick={handleAssignmentSubmit}>Teslim Et</button>
                        </>
                    )}

                    {(status === 'GRADED' || status === 'SUBMITTED') && 
                        <>
                            <div className='unit-body-section'>
                                <label className='unit-section-title'>Eklenen döküman</label>
                                {(assignment.mySubmission && assignment.mySubmission.document) ? (
                                    <Document 
                                    file={assignment.mySubmission.document}
                                    />
                                ) : (
                                    <i>Döküman eklenmedi.</i>
                                )}
                            </div>
                            <div className='unit-body-section'>
                                <label className='unit-section-title'>Teslim notu</label>
                                {(assignment.mySubmission && assignment.mySubmission.comment) ?(
                                    <p className='unit-section-text'>{assignment.mySubmission.comment}</p>
                                ) : (
                                    <i>Not eklenmedi.</i>
                                )}
                            </div>
                        </>
                    }


                    {status === 'SUBMITTED' &&
                        <button className='btn' onClick={handleAssignmentUnsubmit}>Teslimi geri al</button>
                    }
                    {status === 'GRADED' &&
                        <>
                            <div className='unit-body-section'>
                                <label className='unit-section-title'>Ödev sonucu</label>
                                {(assignment.mySubmission && assignment.mySubmission.grade) ? (
                                    <span className='assignment-grade'>{assignment.mySubmission.grade}/100</span>
                                ) : (
                                    <i>Daha sonuçlandırılmadı</i>
                                )}
                            </div>
                            <div className='unit-body-section'>
                                <label className='unit-section-title'>Geri dönüş</label>
                                {(assignment.mySubmission && assignment.mySubmission.feedback) ? (
                                    <p className='unit-section-text'>{assignment.mySubmission.feedback}</p>
                                ) : (
                                    <i> Geri dönüş yapılmadı.</i>
                                )}
                            </div>
                        </>
                    }
                </div>
            )}
            {submitError && <p className='error-message' style={{textAlign : 'center'}}>Ödev teslim edilemedi.</p>}
        </div>
    );
}
export default SingleAssigment;