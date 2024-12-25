import { useState } from 'react';
import './PastHomeworksOfficer.css';
import AssignmentSearch from '../../../../../utils/assignmentSearch/assignmentSearch';
import GradeAssignment from './GradeAssignment';

const PastHomeworksOfficer = () => {
    const [assignments, setAssignments] = useState([]);
    const [isSearched, setIsSearched] = useState(false);

    const handleSearchResults = (response) => {
        setAssignments([]);
        // response = response.filter(assignment => new Date() > new Date(assignment.dueDate));
        setIsSearched(true);
        setAssignments(response);
    }
    const handleAssignmentUpdate = () => {
        
    }

    return(
        <>
            <AssignmentSearch onSearchResults={handleSearchResults} />
            {isSearched && (
                assignments.length > 0 ? (
                    assignments.map((assignment) => (
                        <GradeAssignment
                            key={assignment.id}
                            assignment={assignment}
                            onUpdate={handleAssignmentUpdate}
                        />
                    ))
                ) : (
                    <p>no assignments found</p>
                )
            )}
        </>
    );
}
export default PastHomeworksOfficer;