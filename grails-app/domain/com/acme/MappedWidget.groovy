package com.acme

import com.mongodb.ObjectId

class MappedWidget {

    String _id
    int stockCount
    double length
    String description
    Date createDate
    boolean active
    List stockNumbers
    
    static mongoTypeName = 'mappedwidget'
    static mongoFields = [
        'sc' : 'stockCount',
        'l'  : 'length',
        'd'  : 'description',
        'cd' : 'createDate',
        'a'  : 'active',
        'sn' : 'stockNumbers'
    ]

}
