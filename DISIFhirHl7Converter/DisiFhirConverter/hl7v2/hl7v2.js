// -------------------------------------------------------------------------------------------------
// Copyright (c) Disi-Unibo. All rights reserved.
// Licensed under the MIT License (MIT). See LICENSE in the repo root for license information.
// -------------------------------------------------------------------------------------------------
var specialCharProcessor 		= require('../inputProcessor/specialCharProcessor');
var hl7SequenceHandler 			= require('./hl7EscapeSequence');
var hl7v2TemplatePreprocessor 	= require('../inputProcessor/templatePreprocessor');
var CoverageArray 				= require('./coverage-array');
var dataHandler 				= require('../dataHandler/dataHandler');

module.exports = class hl7v2 extends dataHandler {
    constructor() {
        super("hl7v2");
    }

    parseSrcData(msg) {
        return new Promise((fulfill, reject) => {
            try{
                var data = parseHL7v2(msg);
                fulfill(data);		//fulfill will be deprecated and eventually go away entirely BETTER USE: resolve
            }
            catch (err) {
                reject(err);
            }
        });
    }

    preProcessTemplate(templateStr) {
        return super.preProcessTemplate(hl7v2TemplatePreprocessor.Process(templateStr));
    }

    postProcessResult(inResult) {
        return super.postProcessResult(inResult);
    }

    getConversionResultMetadata(context) {
		console.log("\n============================ hl7v2 getConversionResultMetadata  ");
        return {
            'unusedSegments': parseCoverageReport(context),
            'invalidAccess': parseInvalidAccess(context)
        };
    }
};

function doesElementExistFactory(type, line) {
    return ((element) => element.type === type && element.line === line);
}

function parseHL7v2(msg) {
	console.log("\n============================ hl7v2 PARSING 	");
    var segments = msg.split(/\r?\n/);
    if (segments[0].substring(0, 3) !== "MSH") {
        throw new Error("Invalid HL7 v2 message, first segment id = " + segments[0].substring(0, 3));
    }

    if (segments[0].length < 8) {
        throw new Error("MSH segment missing separators!");
    }

    let separatorsStr = segments[0].substring(3, 8);
    if (new Set(separatorsStr).size !== separatorsStr.length) {
        throw new Error(`Duplicate separators!`);
    }

    //Discover the separators for the segments.
    var fieldSeparator        = segments[0][3];
    var componentSeparator    = segments[0][4];
    var repetitionSeparator   = segments[0][5];
    var escapeCharacter       = segments[0][6];
    var subcomponentSeparator = segments[0][7];

    if (escapeCharacter != '\\') {
        throw Error("Escape character is *not* backslash. This has not been tested!");
    }

    var out     = {};
    out.v2      = {};
    out.v2.data = [];
    out.v2.meta = [];

    for (var i = 0; i < segments.length; i++) {
        var fields = segments[i].split(fieldSeparator);
        if (fields[0].length) // Checking that it is not an empty line at the end of the file. 
        {
            var seg = CoverageArray.makeUndefinedAccessReporterArray(fields[0], i);
            out.v2.meta.push(fields[0]);
            for (var f = 1; f < fields.length; f++) {
                if (i == 0 && f == 1) //First field of the MSH segment lists the separators
                {
                    var separators = CoverageArray.makeCoverageArray();
                    separators.push([fields[f]]);
                    separators.accessed[0] = true;
                    seg.push(separators);
                }
                else {
                    if (fields[f].length == 0) //If the field is not present set null
                    {
                        seg.push(null);
                    }
                    else {
                        var ocomps = CoverageArray.makeCoverageArray();

                        createComponents(ocomps,
                            fields[f],
                            fieldSeparator,
                            componentSeparator,
                            subcomponentSeparator,
                            repetitionSeparator);

                        var reps = fields[f].split(repetitionSeparator);
                        for (var repId = 0; repId < reps.length; repId++) {
                            let repIdArr = [];
                            createComponents(repIdArr,
                                reps[repId],
                                fieldSeparator,
                                componentSeparator,
                                subcomponentSeparator,
                                repetitionSeparator);
                            ocomps.repeats.push(repIdArr);
                        }

                        seg.push(ocomps);
                    }
                }
            }
            out.v2.data.push(seg);
        }
    }
  console.log("hl7v2 out.v2.meta: " 	+ out.v2.meta );		//BY AN ( MSH,EVN,PID,NK1,PV1,GT1,DG1,IN1,IN2,IN1,IN2,IN1)
  console.log("hl7v2 out.v2.data.length:" + out.v2.data.length );		//BY AN (body del msg HL7 privato di MSH|, EVN|, PID|etc)
  //console.log("hl7v2 out.v2.data[11]:" + out.v2.data[11] );		//BY AN (body del msg HL7 privato di MSH|, EVN|, PID|etc)
    return out;
}

function createComponents(containerArr, dataStr, fieldSeparator, componentSeparator, subcomponentSeparator, repetitionSeparator) {
    var comps = dataStr.split(componentSeparator);
    for (var c = 0; c < comps.length; c++) //Loop over components (e.g., '^')
    {
        if (comps[c].length) {
            var sub = comps[c].split(subcomponentSeparator); //Subcomponents (e.g. '&')
            sub = sub.map(x => {
                return specialCharProcessor.Escape(
                    hl7SequenceHandler.Unescape(x, fieldSeparator, componentSeparator, subcomponentSeparator, repetitionSeparator));
            });
            containerArr.push(sub);
        }
        else {
            containerArr.push(null);
        }
    }
}

/*
=============================================================================================
parsedMsg.v2.data	: sembrano quelli di dataContext.msg.v2.data in worker
segmentIndex		: vanno da 0 a 11, come ci si aspetta dal dato HL7 di input
=============================================================================================
*/
function parseCoverageReport(parsedMsg) {
console.log("------- hl7v2 parseCoverageReport ");
//console.log("parsedMsg.v2.data " +  parsedMsg.v2.data );		//BY AN  
    var coverageReport = [];
    var v2 = parsedMsg.v2;
    v2.data.forEach((segment, segmentIndex) => {
////console.log("hl7v2.js parseCoverageReport segment " +  Object.keys(parsedMsg) );				//BY AN  (v2)
////console.log("hl7v2.js parseCoverageReport segmentIndex " +  Object.keys(segmentIndex) );		//BY AN  (-)
//console.log("segmentIndex " +  segmentIndex );						//BY AN  (0 ... 11)
        segment.forEach((component, componentIndex) => {
            if (component) {
//console.log("hl7v2.js parseCoverageReport component " +  Object.keys(component) );		//BY AN  (accessed,repeats,push)
                component.accessed.forEach((record, recordIndex) => {
                    if (!record) {
                        // The index needs to be corrected because the array doesn't contain the first column, so the indexes are one too small.
                        var correctedIndex = componentIndex + 1;

                        if (!coverageReport.some(doesElementExistFactory(v2.meta[segmentIndex], segmentIndex))) {
                            coverageReport.push({
                                type: v2.meta[segmentIndex],
                                line: segmentIndex,
                                field: []
                            });
                        }

                        var fields = coverageReport.find(doesElementExistFactory(v2.meta[segmentIndex], segmentIndex)).field;

                        if (!fields.some((element) => element.index === correctedIndex)) {
                            fields.push({
                                index: correctedIndex,
                                component: []
                            });
                        }

                        var item = { "index": recordIndex };
                        item.value = component[recordIndex] ? component[recordIndex][0] : "";
                        fields.find((element) => element.index === correctedIndex).component.push(item);
                    }
                });
            }
        });
    });

    return coverageReport;
}

function parseInvalidAccess(parsedMsg) {
console.log("------- hl7v2 parseInvalidAccess ");
//console.log("hl7v2.js parseInvalidAccess parsedMsg " +  Object.keys(parsedMsg));	 //v2	 
    var invalidAccesses = [];
    parsedMsg.v2.data.forEach((segment) => {
        segment.undefinedFieldsAccessed.forEach((field) => {
            if (!invalidAccesses.some(doesElementExistFactory(segment.name, segment.line))) {
                invalidAccesses.push({
                    type: segment.name,
                    line: segment.line,
                    field: []
                });
            }

            invalidAccesses.find(doesElementExistFactory(segment.name, segment.line))
                .field.push({ index: (field ? parseInt(field, 10) + 1 : "undefined") });
        });
    });

    return invalidAccesses;
}

module.exports.parseHL7v2 = parseHL7v2;
