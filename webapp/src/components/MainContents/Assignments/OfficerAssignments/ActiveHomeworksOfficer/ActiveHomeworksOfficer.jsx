import './ActiveHomeworksOfficer.css';
import AssignmentSearch from '../../../../../utils/assignmentSearch/assignmentSearch';
import { useState } from 'react';
import UpdateAssignment from './UpdateAssignment/UpdateAssignment';
import NoAssignmentFound from '../../../../common/IconComponents/NoAssignmentFound';
import Loading from '../../../../common/Loading/Loading';

const ActiveHomeworksOfficer = () => {
    const [isSearched, setIsSearched] = useState(false);
    const [searching, setSearching] = useState(false);
    
    const [assignments, setAssignments] = useState([]);

    const handleSearchResults = (response) => {
        setAssignments([]);
        response = response.filter(assignment => new Date(assignment.dueDate) >= new Date());
        setIsSearched(true);
        setAssignments(response);
    };

    const handleAssignmentUpdate = (updatedAssignment, ) => {
        console.log("updated assignment : ",updatedAssignment);
        setAssignments((prevAssignments) => {
            return prevAssignments.map(assignment =>
                assignment.id === updatedAssignment.id ? updatedAssignment : assignment
            )
        });
    }

    return (
        <>
            <AssignmentSearch
                onSearchResults={handleSearchResults}
                onSearch={(value) => setSearching(value)}
                isFuture={true}
            />
            {isSearched && (
                searching ? (<Loading/>) : (
                    assignments.length > 0 ? (
                        assignments.map((assignment) => (
                                <UpdateAssignment
                                    key={assignment.id}
                                    assignment={assignment}
                                    onUpdate={handleAssignmentUpdate}
                                />
                            ))
                        ) : (<NoAssignmentFound/>)
                    )
            )}
        </>
    );
};

export default ActiveHomeworksOfficer;