import { useEffect, useState } from "react";
import useAuth from "../../../hooks/useAuth";
import { getAssignments } from "../../../services/assignmentService";
import Loading from "../../common/Loading/Loading";
import Warning from '../../common/IconComponents/Warning';
import NoResult from "../../common/IconComponents/NoResult";
import SingleAssignmentSummary from "./SingleAssignmentSummary";


const AssignmentSummary = () => {
    const { user } = useAuth();
    const [loading, setLoading] = useState(false);

    const [assignments, setAssignments] = useState([]);
    const [loadError, setLoadError] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try{
                setLoading(true);
                const response = await getAssignments(user.role, user.id);
                user.role === 'ROLE_STUDENT'
                    ? setAssignments(response.data.filter(assignment => (new Date() <= new Date(assignment.dueDate))))
                    : setAssignments(response.data.filter(assignment => (new Date() <= new Date(assignment.dueDate))));
            }catch(error){
                console.log(error);
                setLoadError(true);
            }finally{
                setLoading(false);
            }
        }
        fetchData();
    }, [user]);

    if(loading) return <Loading/>;
    if(loadError) return <Warning/>;

    return(
        assignments.length === 0 ? (<NoResult/>) : (
            assignments.map(assignment => (
                <SingleAssignmentSummary
                    key={assignment.id}
                    assignment={assignment}
                />
            ))
        )
    );
}
export default AssignmentSummary;