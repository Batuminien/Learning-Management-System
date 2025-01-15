import { useEffect, useState } from "react";
import useAuth from "../../../hooks/useAuth";
import { getAllExams, getStudentExams } from "../../../services/pastExamService";
import ReactECharts from 'echarts-for-react';
import Loading from "../../common/Loading/Loading";
import { fetchCssVariable } from "../../../utils/fileUtils";
import { PiUserBold, PiUserFill, PiUsersThreeBold, PiUsersThreeFill } from "react-icons/pi";



const PastExamSummary = () => {
    const { user } = useAuth();

    const [options, setOptions] = useState(null);
    const [personalStats, setPersonalStats] = useState([]);
    const [generalStats, setGeneralStats] = useState([]);


    useEffect(() => {
        const fetchData = async () => {
            try{
                const response = await getStudentExams(user.id)
                
                const personalNetScores = [];
                const generalNetScores = [];
                const categories = [];


                response.data.slice(0,4).forEach(exam => {
                    console.log(exam)
                    categories.push(exam.pastExam.name)
                    generalNetScores.push(exam.pastExam.overallAverage)
                    personalNetScores.push(exam.subjectResults.reduce((sum, subject) => sum + subject.netScore, 0))
                })
                
                console.log(personalNetScores);
                console.log(generalNetScores);
                console.log(categories);

                setPersonalStats(personalNetScores);
                setGeneralStats(generalNetScores);

                const options = {
                    color: [fetchCssVariable('--personal-average'), fetchCssVariable('--general-average')],
                    tooltip: {
                      trigger: 'axis',
                      axisPointer: {
                        type: 'shadow'
                      }
                    },
                    legend: {},
                    grid: {
                      left: '3%',
                      right: '4%',
                      bottom: '3%',
                      containLabel: true
                    },
                    xAxis: [
                      {
                        type: 'category',
                        data: categories
                      }
                    ],
                    yAxis: [
                      {
                        type: 'value'
                      }
                    ],
                    series: [
                      {
                        name: 'Kişisel Net',
                        type: 'bar',
                        data: personalNetScores
                      },
                      {
                        name: 'Ortalama Net',
                        type: 'bar',
                        data: generalNetScores
                      },
                    ]
                };
                setOptions(options);
            }catch(error){
                console.log(error);
            }

        }
        fetchData();
    }, [user]);



    return(
        <>
            {options ? (
                <div className="exam-result-chart">
                    <ReactECharts option={options}/>
                    <div className="average-info">
                        <div className="single-average ">
                            <span className="icon-container personal-average"><PiUserBold size={32}/></span>
                            <span className="table-row-element">Kişisel Ortalama : {personalStats.reduce((acc, stat) => acc + stat, 0) / personalStats.length}</span>
                        </div>

                        <div className="single-average">
                            <span className="icon-container general-average"><PiUsersThreeBold size={32}/></span>
                            <span className="table-row-element">Genel Ortalama : {generalStats.reduce((acc, stat) => acc + stat, 0) / generalStats.length}</span>
                        </div>
                    </div>
                </div>
            ) : (<Loading/>)}
        </>
    )
}
export default PastExamSummary;