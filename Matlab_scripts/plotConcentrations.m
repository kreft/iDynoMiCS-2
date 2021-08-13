function plotConcentrations (folderName)
    
    fileList = dir(fullfile(folderName, '*.csv'));
    
    number = numel(fileList);
    
    firstFile = fullfile(folderName,fileList(1).name);
    firstArray = table2array(readtable(firstFile));
    arraySize = size(firstArray);
    allData = zeros(arraySize(1), arraySize(2), number);
    
    for k = 1:number
       file = fullfile(folderName,fileList(k).name);
       data = table2array(readtable(file));
       allData(:,:,k) = data;
    end
    
    singleLayer = squeeze(allData(1,:,:));
    
    
    surf (singleLayer);
    %zlim([5.995e-6 6.005e-6]);
end