package com.acme

class MappedWidget {

    String _id
    int stockCount
    Double length
    String description
    Date createDate
    boolean active
    List stockNumbers
    Map features
    
    static mongoTypeName = 'mappedwidget'
    static mongoFields = [
        'sc' : 'stockCount',
        'l'  : 'length',
        'd'  : 'description',
        'cd' : 'createDate',
        'a'  : 'active',
        'sn' : 'stockNumbers',
        'f'  : 'features'
    ]

}
