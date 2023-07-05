function analyseConvergence (folderName)
    
    fileList = dir(fullfile(folderName, '*.csv'));
    
    number = numel(fileList);
    norms = zeros(number,1);
    maxes = zeros(number,1);
    mins = zeros(number,1);
    means = zeros(number,1);
    
    for k = 1:number
       file = fullfile(folderName,fileList(k).name);
       data = table2array(readtable(file));
       
       norms(k,:) = norm(data(:));
       maxes(k,:) = max(data(:));
       mins(k,:) = min(data(:));
       means(k,:) = mean(data(:));
    end
    
    plot(norms);
    hold on;
    plot(maxes)
    hold on;
    plot (mins);
    hold on;
    plot (means);
    legend('norm-2','max value','min value', 'mean value');
    hold off;
end