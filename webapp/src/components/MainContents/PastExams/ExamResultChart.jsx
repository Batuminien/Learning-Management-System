import { useEffect, useState } from "react";
import Loading from "../../common/Loading/Loading";
import ReactECharts from 'echarts-for-react';
import { fetchCssVariable } from "../../../utils/fileUtils";


const ExamResultChart = ({subjectResults = []}) => {

    const [options, setOptions] = useState(null);

    useEffect(() => {
        const categories = [];
        const correctAnswers = [];
        const wrongAnswers = [];
        const blankAnswers = [];
        const netScores = [];
        const sortedSubjectResults = subjectResults.sort((a, b) => b.subjectName.localeCompare(a.subjectName)); 

        sortedSubjectResults.forEach(subject => {
            categories.push(subject.subjectName);
            correctAnswers.push(subject.correctAnswers);
            wrongAnswers.push(subject.incorrectAnswers);
            blankAnswers.push(subject.blankAnswers);
            netScores.push(subject.netScore );
        })
        const options = {
            color: [fetchCssVariable('--correctAnswer'), fetchCssVariable('--wrongAnswer'), fetchCssVariable('--blankAnswer'), fetchCssVariable('--netScore')], // Custom color palette
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
            yAxis: [
              {
                type: 'category',
                data: categories
              }
            ],
            xAxis: [
              {
                type: 'value'
              }
            ],
            series: [
              {
                name: 'Doğru',
                type: 'bar',
                stack: 'Ad',
                data: correctAnswers
              },
              {
                name: 'Yanlış',
                type: 'bar',
                stack: 'Ad',
                data: wrongAnswers
              },
              {
                name: 'Boş',
                type: 'bar',
                stack: 'Ad',
                data: blankAnswers
              },
              {
                name: 'Net',
                type: 'bar',
                data: netScores
              }
            ]
          };
          setOptions(options);
    }, [subjectResults]);

    return(
        <>
            {options ? (
                <div className="exam-result-chart">
                    <ReactECharts option={options}/>
                </div>
            ) : (<Loading/>)}
        </>
    )
}
export default ExamResultChart;