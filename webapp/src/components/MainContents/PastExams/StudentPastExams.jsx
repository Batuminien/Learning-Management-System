import { useEffect, useState } from "react";
import { getStudentExams } from "../../../services/pastExamService";
import Loading from "../../common/Loading/Loading";
import NoResult from "../../common/IconComponents/NoResult";
import SingleExam from "./SingleExam";



const StundentPastExams = ({user}) => {

    const [loading, setLoading] = useState(false);
    const [exams, setExams] = useState([]);
    
    useEffect(() => {
        const fetchData = async () => {
            try{
                setLoading(true);
                const response = await getStudentExams(user.id);
                setExams(response.data);
            }catch(error){
                console.log(error);
            }finally{
                setLoading(false);
            }
        }
        fetchData();
    }, []);

    if(loading) return <Loading/>;

    return(
        <>
            {exams.length === 0 ? (<NoResult/>) : (
                exams.map(exam => (
                    <SingleExam exam={exam} key={exam.id}/>
                ))
            )}
        </>
    );
}
export default StundentPastExams;