package com.example.estsharabot.model

class ImageReport(
    var organ: String, var disease: String, var percentage: String
) {

    var imageURL: String = ""

    constructor(organ: String, disease: String, percentage: String, imageURL: String) : this(
        organ,
        disease,
        percentage
    ) {
        this.imageURL = imageURL

    }
}