import { useContext, useState } from 'react';

import './SingleAssignment.css';
import { downloadDocument, submitAssignment, unsubmitStudentAssignment } from '../../../../../services/assignmentService';
import { AuthContext } from '../../../../../contexts/AuthContext';
import Document from '../../../../common/Document/Document';

const SingleAssigment = ({ assignment, refreshAssignments, status }) => {
    const { user } = useContext(AuthContext);

    const [isExpanded, setIsExpanded] = useState(false);
    const [submitError, setSubmitError] = useState(false);
    const [uploadedFile, setUploadedFile] = useState(null);
    const [submissionComment, setSubmissionComment] = useState('');

    console.log(assignment);
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
        console.log('handling assignment unsubmit');
        unsubmitStudentAssignment(assignment.id, user.accessToken)
            .then(response => {
                console.log(response);
                refreshAssignments();
            })
            .catch(error => {
                console.log(error);
            })

    }

    // TODO
    const handleDocumentDownload = async () => {
        console.log('handling document download');
        console.log(assignment.teacherDocument);
        try {
            const response = await downloadDocument(assignment.teacherDocument.documentId, user.accessToken);
            console.log(response);
            const objectURL = URL.createObjectURL(response);
            const a = document.createElement('a');
            a.href = objectURL;
            // a.download = 
        }catch(error) {
            console.log(error);
        }
    }

    return(
        <div className='assignment-container'>
            <div className='assignment-header' onClick={() => setIsExpanded((prev) => !prev)}>
                <div className='assignment-header-info'>
                    <img src='https://placeholder.pics/svg/32x32' alt='icon' />
                    <span className='assignment-subject'>{assignment.courseName}</span>
                    <span className='assignment-title'>{assignment.title}</span>
                    <span className='assignment-dueDate'>{(new Date(assignment.createdDate)).toLocaleDateString('en-GB')} - {(new Date(assignment.dueDate)).toLocaleDateString('en-GB')}</span>
                </div>
                <button className='expand-btn'>
                    <img src={isExpanded ? 'icons/arrow-up.svg' : 'icons/arrow-down.svg'} alt='toggle assignment details' />
                </button>
            </div>
            {isExpanded && (
                <div className='assignment-body'>
                    <div style={{border : '1px solid grey'}}></div>

                    <div className='assignment-body-section'>
                        <label className='assignment-section-title'>Açıklama</label>
                        {assignment.description ? (
                            <p className='assignment-section-text'>{assignment.description}</p>
                        ) : (
                            <i className='assignment-section-text'>Açıklama yok.</i>
                        )}
                    </div>

                    <div className='assignment-body-section'>
                        <label className='assignment-section-title'>Yardımcı materyaller</label>
                        {assignment.teacherDocument ? (
                            <Document 
                                file={assignment.teacherDocument}
                            />
                        ) : (
                            <i className='assignment-section-text'>Döküman eklenmedi.</i>
                        )}
                    </div>
                    
                    {status === 'PENDING' && (
                        <>
                            <div className='assignment-body-section'>
                                {uploadedFile ? (
                                    <>
                                        <label className='assignment-section-title'>Yüklenen döküman</label>
                                        <Document 
                                            file={uploadedFile}
                                            isRemovable={true}
                                            onRemove={() => setUploadedFile(null)}
                                        />
                                    </>
                                ) : (
                                    <>
                                        <label className='assignment-section-title'>Döküman ekle</label>
                                        <input type='file' onChange={handleFileUpload}/>
                                    </>
                                )}
                            </div>
                            <div className='assignment-body-section'>
                                <label className='assignment-section-title'>Teslim notu</label>
                                <div className='input-container'>
                                    
                                <textarea
                                    className='input'
                                    type='textarea'
                                    style={{resize : 'none', width : '50%', aspectRatio : 5}}
                                    value={submissionComment}
                                    onChange={(event) => setSubmissionComment(event.target.value)}
                                    />
                                    </div>
                            </div>
                            <button className='btn' onClick={handleAssignmentSubmit}>Teslim Et</button>
                        </>
                    )}

                    {(status === 'GRADED' || status === 'SUBMITTED') && 
                        <>
                            <div className='assignment-body-section'>
                                <label className='assignment-section-title'>Eklenen döküman</label>
                                {(assignment.mySubmission && assignment.mySubmission.document) ? (
                                    <Document 
                                    file={assignment.mySubmission.document}
                                    />
                                ) : (
                                    <i>Döküman eklenmedi.</i>
                                )}
                            </div>
                            <div className='assignment-body-section'>
                                <label className='assignment-section-title'>Teslim notu</label>
                                {(assignment.mySubmission && assignment.mySubmission.comment) ?(
                                    <p className='assignment-section-text'>{assignment.mySubmission.comment}</p>
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
                            <div className='assignment-body-section'>
                                <label className='assignment-section-title'>Ödev sonucu</label>
                                {(assignment.mySubmission && assignment.mySubmission.grade) ? (
                                    <span className='assignment-grade'>{assignment.mySubmission.grade}/100</span>
                                ) : (
                                    <i>Daha sonuçlandırılmadı</i>
                                )}
                            </div>
                            <div className='assignment-body-section'>
                                <label className='assignment-section-title'>Geri dönüş</label>
                                {(assignment.mySubmission && assignment.mySubmission.feedback) ? (
                                    <p className='assignment-section-text'>{assignment.mySubmission.feedback}</p>
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