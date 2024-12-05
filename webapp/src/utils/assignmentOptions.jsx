import NewAssignment from "../components/MainContents/Assignments/OfficerAssignments/NewAssignment/NewAssignment";
import ActiveHomeworksOfficer from "../components/MainContents/Assignments/OfficerAssignments/ActiveHomeworksOfficer/ActiveHomeworksOfficer";
import PastHomeworksOfficer from "../components/MainContents/Assignments/OfficerAssignments/PastHomeworksOfficer/PastHomeworksOfficer";


const assignmentOptions = {
    ROLE_OFFICER: [
        { title: 'Ödev Ekle', component: NewAssignment},
        { title: 'Aktif Ödevler', component: ActiveHomeworksOfficer },
        { title: 'Geçmiş Ödevler', component: PastHomeworksOfficer },
    ],
    ROLE_STUDENT: [
        { 
            title: 'Aktif Ödevler',
            status : 'PENDING',
            index : 0,
            compare : (assignment) =>
                        !assignment.mySubmission &&
                        ((new Date(assignment.dueDate)) >= (new Date()))
        },
        {
            title: 'Gönderilmiş',
            status : 'SUBMITTED',
            index : 1,
            compare : (assignment) =>
                        assignment.mySubmission &&
                        assignment.mySubmission.status === 'SUBMITTED' &&
                        ((new Date(assignment.dueDate)) >= (new Date()))
        },
        {
            title: 'Geçmiş Ödevler',
            status : 'GRADED',
            index : 2,
            compare : (assignment) => 
                        ((new Date(assignment.dueDate)) < (new Date()))
        },
    ]
}

export const getAssignmentOptions = (role) => {
    return assignmentOptions[role];
}