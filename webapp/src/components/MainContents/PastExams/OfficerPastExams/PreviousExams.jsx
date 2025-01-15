import React, { useEffect, useState } from 'react';
import Select from 'react-select';
import DateInput from '../../../common/DateInput';
import { fetchCssVariable } from '../../../../utils/fileUtils';
import { getAllExams } from '../../../../services/pastExamService';
import Warning from '../../../common/IconComponents/Warning';
import Loading from '../../../common/Loading/Loading';
import NoResult from '../../../common/IconComponents/NoResult';
import SingleOfficerExam from './SingleOfficerExam';
import { examTypeOptions } from './OfficerPastExams';

const PreviousExams = () => {
    const [warning, setWarning] = useState(false);
    const [loading, setLoading] = useState(false);
    const [filtering, setFiltering] = useState(false);

    const [searchError, setSearchError] = useState(false);
    const [isSearched, setIsSearched] = useState(false);

    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [selectedExamTypes, setSelectedExamTypes] = useState([]);

    const [allExams, setAllExams] = useState([]);
    const [searchedExams, setSearchedExams] = useState([]);

    useEffect(() => {
        const fetchExams = async () => {
            try{
                setLoading(true);
                const response = await getAllExams();
                console.log(response.data);
                setAllExams(response.data);
            }catch(error){
                console.log(error);
                setWarning(true);
            }finally{
                setLoading(false);
            }
        }
        fetchExams();
    }, []);

    

    const handleSearch = () => {
        setSearchError(false);
        if(startDate && endDate && new Date(startDate) > new Date(endDate)){
            setSearchError(true);
            return;
        }
        setIsSearched(true);
        setFiltering(true);
        setSearchedExams([]);

        let filterResult = allExams;

        if(selectedExamTypes.length !== 0) {filterResult = filterResult.filter(exam => selectedExamTypes.some(examType => examType.value === exam.examType));}
        if(startDate) {filterResult = filterResult.filter(exam => new Date(exam.results[0].pastExam.date) >= new Date(startDate))}
        if(endDate) {filterResult = filterResult.filter(exam => new Date(exam.results[0].pastExam.date) <= new Date(endDate))}

        console.log('all exams after filter : ', filterResult);
        setSearchedExams(filterResult);
        setFiltering(false);
    }

    if(loading) return <Loading/>;
    if(warning) return <Warning/>;

    return(
        <>
            <div className="search">
                <div className="search-options">
                    <div className="input-container">
                        <label className="label">Sınav Türü</label>
                        <Select
                            styles={{
                                control: (base) => ({
                                    ...base,
                                    backgroundColor : fetchCssVariable('--input-background')
                                })
                            }}
                            options={examTypeOptions}
                            closeMenuOnSelect={false}
                            isMulti
                            value={selectedExamTypes}
                            onChange={selected => setSelectedExamTypes(selected)}
                        />
                    </div>
                    <DateInput
                        title='Başlangıç Tarihi'
                        onInput={(date) => setStartDate(date)}
                    />
                    <DateInput
                        title='Bitiş Tarihi'
                        onInput={(date) => setEndDate(date)}
                    />
                    <button className="btn" onClick={handleSearch}>Ara</button>
                    {searchError && <p className='error-message'>Başlangıç tarihi bitiş tarihinden sonra olamaz!</p>}
                </div>
            </div>
            {isSearched && (
                filtering ? (<Loading/>) : (
                searchedExams.length === 0 ? (<NoResult/>) : (
                    searchedExams.map(exam => (
                        <SingleOfficerExam key={exam.id} exam={exam}/>
                    ))
                ))
            )}

        </>
    );
}
export default PreviousExams;