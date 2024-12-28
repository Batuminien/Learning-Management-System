import './StudentAttendance.css';

import { getAttendanceOfStudent, getAttendanceStatsOfStudent } from "../../../../services/attendanceService";
import { AuthContext } from "../../../../contexts/AuthContext";
import { getStudentClass } from "../../../../services/classesService";

import SubjectAttendance from "./SubjectAttendance";
import DateInput from "../../../common/DateInput/DateInput";

import React from 'react';
import jsPDF from "jspdf";
import html2canvas from "html2canvas";
import { useContext, useEffect, useState } from "react";
import { fetchCssVariable } from '../../../../utils/fileUtils';

import { robotoFont } from '../../../../../public/Roboto-Regular-normal';
import Loading from '../../../common/Loading/Loading';

const StudentAttendance = () => {
    const [isLoading, setIsLoading] = useState(false);

    const { user } = useContext(AuthContext);
    const [studentClass, setStudentClass] = useState('');

    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const [statsBySubject, setStatsBySubject] = useState({});
    const [historyBySubjects, setHistoryBySubjects] = useState({});
    
    const [isSearched, setIsSearched] = useState(false);
    const [searchError, setSearchError] = useState('');

    const [requestReload, setRequestReload] = useState(false);

    const [isGeneratingReport, setIsGeneratingReport] = useState(false);
    
    useEffect(() => {
        getStudentClass(user.accessToken)
            .then(response => {
                console.log("user class : ", response);
                setStudentClass(response.data.classId);
            })
            .catch(error => {
                setRequestReload(true);
            })
    }, []);

    const handleSearch = async () => {
        setSearchError('');
        if((new Date(startDate)) > (new Date(endDate))){
            setSearchError('Başlangıç tarihi bitiş tarihinden önce olmalıdır.');
            return;
        }
        try {
            setIsLoading(true);
            setIsSearched(true);
            const params = {};
            params.startDate = (startDate ? startDate : gettwoyearsBefore());
            params.endDate = endDate ? endDate : (new Date().toISOString().split('T')[0]);
            console.log(params)
            const attendanceHistoryBySubjects = {};
            const historyResponse = await getAttendanceOfStudent(user.id, params, user.accessToken);
            console.log(historyResponse)
            historyResponse.data.forEach(attendance => {
                if(!attendanceHistoryBySubjects[attendance.courseId]){
                    attendanceHistoryBySubjects[attendance.courseId] = [];
                }
                attendanceHistoryBySubjects[attendance.courseId].push(attendance);
            })
            console.log(attendanceHistoryBySubjects);
            setHistoryBySubjects(attendanceHistoryBySubjects);

            const attendanceStatsBySubjects = {};
            const statsResponse = await getAttendanceStatsOfStudent(user.id, studentClass, user.accessToken);
            statsResponse.data.forEach(stat => {
                attendanceStatsBySubjects[stat.courseId] = stat;
            })
            console.log(attendanceStatsBySubjects);
            setStatsBySubject(attendanceStatsBySubjects);
        }catch(error) {
            setRequestReload(true);
        }finally {
            setIsLoading(false);
        }
    }

    const gettwoyearsBefore = () => {
        const currentDate = new Date();
        currentDate.setFullYear(currentDate.getFullYear() - 2);
        const twoYearsAgo = currentDate.toISOString().split('T')[0];
        return twoYearsAgo;
    }

    const handleReportRequest = async () => {
        setIsGeneratingReport(true);
    
        // Wait for the next render cycle to ensure all components update
        await new Promise(resolve => setTimeout(resolve, 0));
    
        const containers = document.querySelectorAll('.assignment-container');
    
        const pdf = new jsPDF();
        pdf.addFileToVFS('Roboto-Regular-normal.ttf', robotoFont);
        pdf.addFont('Roboto-Regular-normal.ttf', 'Roboto-Regular', 'normal');
        pdf.setFont('Roboto-Regular', 'normal');

        pdf.setFontSize(26);
        pdf.setTextColor(fetchCssVariable('--title'));
    
        const title = 'Yoklama Raporu';
        const x = 105;
        const y = 20;
        pdf.text(title, x, y, { align: 'center' });
    
        const textWidth = pdf.getTextWidth(title);
        const lineY = y + 2;
        pdf.line(x - textWidth / 2, lineY, x + textWidth / 2, lineY);

        pdf.setFontSize(12);
        pdf.setTextColor(fetchCssVariable('--black'));
        pdf.text(`Öğrenci : ${user.name + ' ' + user.surname}`, 10, 30);
        pdf.text(`Rapor verileri aralığı : ${startDate ? (new Date(startDate)).toLocaleDateString('en-GB') : '...'} - ${endDate ? (new Date(endDate)).toLocaleDateString('en-GB') : '...'}`, 10, 35);
        pdf.text(`Raporun oluşturulduğu tarih : ${(new Date()).toLocaleDateString('en-GB')}`, 10, 40);
    
        let yOffset = 50; // Start content after additional lines
    
        for (let i = 0; i < containers.length; i++) {
            const container = containers[i];
            try {
                const canvas = await html2canvas(container, { scale: 2 });
                const imgData = canvas.toDataURL('image/png');
    
                const imgWidth = 190; // Width of the image
                const pageHeight = 297; // A4 page height in mm
                const imgHeight = (canvas.height * imgWidth) / canvas.width;
    
                if (yOffset + imgHeight > pageHeight) {
                    pdf.addPage();
                    yOffset = 10; // Reset Y-offset for the new page
                }
    
                pdf.addImage(imgData, 'PNG', 10, yOffset, imgWidth, imgHeight);
                yOffset += imgHeight + 10;
            } catch (error) {
                console.error('Error exporting element to PDF:', error);
            }
        }
    
        pdf.save(`${user.name}_${user.surname}_yoklama_raporu.pdf`);
        setIsGeneratingReport(false);
    };
    
    
    

    if(requestReload) {
        return(<p>something went wrong, please reload...</p>);
    }

    if(isLoading) {return(<Loading/>)}

    return(
        <>
            <div className="search">
                <div className="search-options">
                    <DateInput 
                        title='Başlangıç Tarihi'
                        onInput={(date) => {setStartDate(date), setSearchError('')}}
                        />
                    <DateInput 
                        title='Bitiş Tarihi'
                        onInput={(date) => {setEndDate(date), setSearchError('')}}
                    />
                    <button className="btn" onClick={handleSearch}>Ara</button>
                </div>
                {searchError && <p className='error-message'>{searchError}</p>}
            </div>

            {isSearched && (
                <>
                    {Object.keys(statsBySubject).map(key => (
                        <SubjectAttendance
                            key={key}
                            stats={statsBySubject[key]}
                            history={historyBySubjects[key]}
                            forceExpand={isGeneratingReport}
                        />
                    ))}
                    <button className='btn' onClick={handleReportRequest}>Rapor oluştur</button>
                </>
            )}
        </>
    );
}
export default StudentAttendance;