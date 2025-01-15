import useAuth from "../../../hooks/useAuth";




const SingleAssignmentSummary = ({assignment}) => {
    const { user } = useAuth();

    console.log(assignment)


    return(
            <div className="unit-container">
                <div className="unit-header-info">
                    <div className="assignment-summary-info">
                        <span className="unit-subject">{assignment.courseName}</span>
                        <span className="unit-subject">{assignment.title}</span>
                        <span className="unit-subject">{(new Date(assignment.createdDate)).toLocaleDateString("en-GB")} - {(new Date(assignment.dueDate)).toLocaleDateString("en-GB")}</span>
                    </div>
                    {user.role === 'ROLE_STUDENT' && (
                        assignment.mySubmission.status === 'PENDING' ? (
                            <span className="assignment-status submitted">Tamamlandı</span>
                        ) : (
                            <span className="assignment-status non-submitted">Tamamlanmadı</span>
                        )
                    )}
                </div>

            </div>
    );
}
export default SingleAssignmentSummary;