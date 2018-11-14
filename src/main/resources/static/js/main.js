'use strict';

var uploadForm = document.querySelector('#uploadForm');
//var fileUploadInput = document.querySelectorAll("[name='files']");
var fileUploadInput = document.getElementsByName('files');
var fileUploadError = document.querySelector('#fileUploadError');
var fileUploadSuccess = document.querySelector(
    '#fileUploadSuccess');

/*var multipleUploadForm = document.querySelector('#multipleUploadForm');
var multipleFileUploadInput = document.querySelector(
    '#multipleFileUploadInput');
var multipleFileUploadError = document.querySelector(
    '#multipleFileUploadError');
var multipleFileUploadSuccess = document.querySelector(
    '#multipleFileUploadSuccess');*/

function uploadFile(file, file2) {
  var formData = new FormData();
  formData.append("file", file);
  formData.append("file2", file2);
  var xhr = new XMLHttpRequest();
  xhr.open("POST", "/process-ht/uploadFile");

  xhr.onload = function () {
    console.log(xhr.responseText);
    var response = JSON.parse(xhr.responseText);
    if (xhr.status == 200) {
      fileUploadError.style.display = "none";
      fileUploadSuccess.innerHTML = "<p>File Processed Successfully.</p><p>DownloadUrl : <a href='"
          + response.fileDownloadUri + "' target='_blank'>"
          + response.fileDownloadUri + "</a></p>";
      fileUploadSuccess.style.display = "block";
    } else {
      fileUploadSuccess.style.display = "none";
      fileUploadError.innerHTML = (response && response.message)
          || "Some Error Occurred";
      fileUploadError.style.display = "block";
    }
  }

  xhr.send(formData);

  fileUploadSuccess.innerHTML = "<img src='images/ajax-loader.gif' alt=''>";
  fileUploadSuccess.style.display = "block";

}

uploadForm.addEventListener('submit', function (event) {
  var hmFile = fileUploadInput[0].files;
  if (hmFile.length === 0) {
    fileUploadError.innerHTML = "Please select human translation file";
    fileUploadError.style.display = "block";
  }
  var mtFile = fileUploadInput[1].files;
  if (mtFile.length === 0) {
    fileUploadError.innerHTML = "Please select machine translation file";
    fileUploadError.style.display = "block";
  }
  uploadFile(hmFile[0], mtFile[0]);
  event.preventDefault();
}, true);

/*function uploadMultipleFiles(files) {
    var formData = new FormData();
    for(var index = 0; index < files.length; index++) {
        formData.append("files", files[index]);
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadMultipleFiles");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
            multipleFileUploadError.style.display = "none";
            var content = "<p>All Files Uploaded Successfully</p>";
            for(var i = 0; i < response.length; i++) {
                content += "<p>DownloadUrl : <a href='" + response[i].fileDownloadUri + "' target='_blank'>" + response[i].fileDownloadUri + "</a></p>";
            }
            multipleFileUploadSuccess.innerHTML = content;
            multipleFileUploadSuccess.style.display = "block";
        } else {
            multipleFileUploadSuccess.style.display = "none";
            multipleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
}

multipleUploadForm.addEventListener('submit', function(event){
    var files = multipleFileUploadInput.files;
    if(files.length === 0) {
        multipleFileUploadError.innerHTML = "Please select at least one file";
        multipleFileUploadError.style.display = "block";
    }
    uploadMultipleFiles(files);
    event.preventDefault();
}, true);*/

