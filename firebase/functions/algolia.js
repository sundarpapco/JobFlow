// Upgraded to modern v4/v5 algoliasearch syntax
const { algoliasearch } = require("algoliasearch");
const logger = require("firebase-functions/logger");

// Instantiating the client using the newer API signature
const algoliaClient = algoliasearch('72NK3L9K3Y', 'c10a09b1161e8ab661e4786c81c8ff5f');

exports.handlePrintOrderCreation = (data, destination) => {
    if (data.previousDestinationId.trim() === "") {
        // User created a new Print Order
        // In modern Algolia, methods are called directly on the client targeting the index
        logger.log("Handling new print order creation - creation record");
        algoliaClient.saveObject({
            indexName: 'completed_jobs',
            body: creationRecord(data, destination)
        });
    } else {
        // This PrintOrderHasMoved from another Destination. Just update the destination
        logger.log("Handling movement of print order - movement record");
        algoliaClient.partialUpdateObject({
            indexName: 'completed_jobs',
            objectID: `po${data.printOrderNumber}`,
            attributesToUpdate: movementRecord(data, destination)
        });
    }
};

exports.handlePrintOrderUpdation = (before, after, destination) => {
    // Update the search record only if the user has updated the printorder.
    if (before.listPosition === after.listPosition) {
        algoliaClient.saveObject({
            indexName: 'completed_jobs',
            body: creationRecord(after, destination)
        });
    }
};

/**
 * Deletes a job index from Algolia when removed from Firestore.
 * Uses modern Algolia v5 Client API syntax.
 * @param {Object} deletedData The snapshot data of the deleted document.
 */
exports.handlePrintOrderDeletion = (deletedData) => {
  // Ensure we match the exact ID structure used during creation ("po" + number)
  const targetObjectID = `po${deletedData.printOrderNumber}`;

  algoliaClient.deleteObject({
    indexName: "completed_jobs",
    objectID: targetObjectID,
  });
};

function creationRecord(data, destination) {
    let partDispatches = [];

    if (Object.prototype.hasOwnProperty.call(data, 'partialDispatches'))
        partDispatches = preparePartialDispatches(data.partialDispatches);
    
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
        destinationId: destination,
        partialDispatches: partDispatches
    };
}

function movementRecord(data, destination) {
    let partDispatches = [];

    if (Object.prototype.hasOwnProperty.call(data, 'partialDispatches'))
        partDispatches = preparePartialDispatches(data.partialDispatches);
    
    return {
        destinationId: destination,
        invoiceDetails: data.invoiceDetails,
        partialDispatches: partDispatches
    };
}

function preparePartialDispatches(dispatches) {
    let partialDispatches = [];
    dispatches.forEach(dispatch => {
        partialDispatches.push(dispatch.invoiceNumber);
    });
    return partialDispatches;
}

function extractPaperDetail(data) {
    const trimHeight = data.plateMakingDetail.trimmingHeight / 10;
    const trimWidth = data.plateMakingDetail.trimmingWidth / 10;
    let roundedPaperHeight = 0.0;
    let roundedPaperWidth = 0.0;
    let sheets = 0;
    let landscapeSteps = 0;
    let portraitSteps = 0;
    let stepsInSheet = 0;
    let gsm = 0;
    let paperName = "";
    let paperDetail = "";

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
        roundedPaperHeight = Math.round(paper.height * 10) / 10;
        roundedPaperWidth = Math.round(paper.width * 10) / 10;
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