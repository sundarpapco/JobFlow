const algoliaSearch = require("algoliasearch");
const algoliaClient = algoliaSearch('72NK3L9K3Y', 'c10a09b1161e8ab661e4786c81c8ff5f');
const algoliaIndex = algoliaClient.initIndex('completed_jobs');


exports.handlePrintOrderCreation = (data,destination) =>{

    if(data.previousDestinationId.trim() == ""){
        //User created a new Print Order
        algoliaIndex.saveObject(creationRecord(data,destination));
    }else{
        //This PrintOrderHasMoved from another Destination. So, its enough if we update the destination alone
        algoliaIndex.partialUpdateObject(movementRecord(data,destination));
    }

};

exports.handlePrintOrderUpdation = (before,after,destination) =>{

    //Two possibilities. The user has updated the print order or just dragged the printorder to rearrange in the list
    //When updating a Print order either the user update the printorder manually, or update is caused by dragging. Both cannot happen at same time
    // Update the search record only if the user has updated the printorder. Else we can just ignore the drag operation
    if(before.listPosition == after.listPosition){
        //The user has updated the printOrder. Need to update the entire search record as anything might have changed
        algoliaIndex.saveObject(creationRecord(after,destination));
    }

};

function creationRecord(data, destination) {

    return {

        objectID: `po${data.printOrderNumber}`,
        printOrderNumber: data.printOrderNumber,
        billingName: data.billingName,
        jobName: data.jobName,
        creationTime: data.creationTime,
        plateNumber: data.plateMakingDetail.plateNumber,
        paperDetail: extractPaperDetail(data),
        invoiceDetails: data.invoiceDetails,
        colours: data.printingDetail.colours,
        printingInstructions: data.printingDetail.printingInstructions,
        destinationId: destination

    };
}


function movementRecord(data,destination){

    return {

        objectID: `po${data.printOrderNumber}`,
        destinationId: destination,
        invoiceDetails: data.invoiceDetails

    };
}


function extractPaperDetail(data) {

    const trimHeight = data.plateMakingDetail.trimmingHeight / 10;
    const trimWidth = data.plateMakingDetail.trimmingWidth / 10;
    var roundedPaperHeight=0.0;
    var roundedPaperWidth=0.0;
    var sheets = 0;
    var landscapeSteps = 0;
    var portraitSteps = 0;
    var stepsInSheet = 0;
    var gsm = 0;
    var paperName = "";
    var paperDetail = "";

    if (data.paperDetails.length > 1) {
        gsm = 0;
        paperName = "";
    } else {
        gsm = data.paperDetails[0].gsm;
        paperName = data.paperDetails[0].name;
    }

    data.paperDetails.forEach(paper => {
        /*We are rounding the paper heigth and width to one digit precision because 
        sometimes the float 76.2 will be saved as 76.199902, and it will cause problem when calculating steps.
        Say 76.2 paper size and 76.2 trimming size, then instead of 1 step, it will be calculated as 0 step cause
        76.1999902 paper size is smaller than 76.2 trimming size
        */
        roundedPaperHeight = Math.round(paper.height*10)/10;
        roundedPaperWidth = Math.round(paper.width*10)/10;
        landscapeSteps = Math.floor(roundedPaperHeight / trimHeight) * Math.floor(roundedPaperWidth / trimWidth);
        portraitSteps = Math.floor(roundedPaperHeight / trimWidth) * Math.floor(roundedPaperWidth / trimHeight);
        stepsInSheet = Math.max(landscapeSteps, portraitSteps);
        sheets = sheets + (paper.sheets * stepsInSheet);
    });

    if (gsm > 0) {
        paperDetail = `${trimHeight} X ${trimWidth} Cm ${gsm} GSM ${paperName} - ${sheets} Sheets`;
    } else {
        paperDetail = `${trimHeight} X ${trimWidth} Cm - ${sheets} Sheets`;
    }

    return paperDetail;

}



