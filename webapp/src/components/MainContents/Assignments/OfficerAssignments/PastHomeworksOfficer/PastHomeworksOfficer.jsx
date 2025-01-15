import { useState } from 'react';
import './PastHomeworksOfficer.css';
import AssignmentSearch from '../../../../../utils/assignmentSearch/assignmentSearch';
import GradeAssignment from './GradeAssignment';
import NoAssignmentFound from '../../../../common/IconComponents/NoAssignmentFound';
import Loading from '../../../../common/Loading/Loading';

const PastHomeworksOfficer = () => {
    const [assignments, setAssignments] = useState([]);
    const [isSearched, setIsSearched] = useState(false);
    const [searching, setSearching] = useState(false);

    const handleSearchResults = (response) => {
        setAssignments([]);
        response = response.filter(assignment => new Date() > new Date(assignment.dueDate));
        setIsSearched(true);
        setAssignments(response);
    }
    const handleAssignmentUpdate = () => {
        
    }

    return(
        <>
            <AssignmentSearch
                onSearchResults={handleSearchResults}
                onSearch={(value) => setSearching(value)}
                isFuture={false}    
            />
            {isSearched && (
                searching ? (<Loading/>) : (
                    assignments.length > 0 ? (
                        assignments.map((assignment) => (
                            <GradeAssignment
                            key={assignment.id}
                            assignment={assignment}
                            onUpdate={handleAssignmentUpdate}
                            />
                        ))
                    ) : (
                        <NoAssignmentFound/>
                    )
                )
            )}
        </>
    );
}
export default PastHomeworksOfficer;