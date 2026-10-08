package com.madak.spices.designsystem.component

/** Attribution for the bundled product photos (Flickr via Openverse, CC BY / CC BY-SA). */
object PhotoCredits {
    data class Credit(val key: String, val title: String, val author: String, val license: String, val url: String)

    val all = listOf(
        Credit("cumin", "Cumin", "Gusjer", "CC BY 2.0", "https://www.flickr.com/photos/20244534@N00/4410692457"),
        Credit("black_pepper", "peppercorn pyramid", "jlodder", "CC BY 2.0", "https://www.flickr.com/photos/45040421@N06/26611981037"),
        Credit("turmeric", "Turmeric Root", "Steenbergs", "CC BY 2.0", "https://www.flickr.com/photos/50310535@N03/6865121460"),
        Credit("paprika", "paprika", "notafish", "CC BY-SA 2.0", "https://www.flickr.com/photos/67853626@N00/105526176"),
        Credit("ginger", "Ginger root", "Kjokkenutstyr.net", "CC BY-SA 2.0", "https://www.flickr.com/photos/146966953@N02/37310945730"),
        Credit("cinnamon", "Cinnamon", "Kjokkenutstyr.net", "CC BY-SA 2.0", "https://www.flickr.com/photos/146966953@N02/37641954626"),
        Credit("ras_el_hanout", "moroccan spice mix", "anathea", "CC BY 2.0", "https://www.flickr.com/photos/39399777@N00/377560861"),
        Credit("spice_mix", "Spices", "col.hou", "CC BY 2.0", "https://www.flickr.com/photos/57705427@N00/5040509915"),
        Credit("herbs", "Bayleaf, Dried Oregano, Crushed Chilies, Fresh Thyme", "Jason Sandeman", "CC BY 2.0", "https://www.flickr.com/photos/8853180@N02/5662765532"),
        Credit("coriander", "coriander seeds", "yoppy", "CC BY 2.0", "https://www.flickr.com/photos/44124362019@N01/554209128"),
        Credit("cloves", "Cloves", "Elenadan", "CC BY 2.0", "https://www.flickr.com/photos/91536681@N00/2398762390"),
        Credit("chili", "Chili Powder [59/366]", "timsackton", "CC BY-SA 2.0", "https://www.flickr.com/photos/43581314@N08/6793974232"),
        Credit("saffron", "Saffron", "geishaboy500", "CC BY 2.0", "https://www.flickr.com/photos/49503154413@N01/4387146766"),
        Credit("nigella", "black cumin", "seelensturm", "CC BY 2.0", "https://www.flickr.com/photos/61404197@N00/4742852418"),
        Credit("fenugreek", "Fenugreek (Mehthi)", "ajay_suresh", "CC BY 2.0", "https://www.flickr.com/photos/83136374@N05/49695827261"),
        Credit("sesame", "Can you tell me how to get to sesame seeds?", "quinn.anya", "CC BY-SA 2.0", "https://www.flickr.com/photos/53326337@N00/2913060197"),
        Credit("cardamom", "Black vs green cardamom", "Ronald van der Graaf", "CC BY 2.0", "https://www.flickr.com/photos/91830216@N03/51753435759"),
        Credit("star_anise", "Star Anise Series", "geishaboy500", "CC BY 2.0", "https://www.flickr.com/photos/49503154413@N01/4297746909"),
        Credit("bay_leaf", "bay leaves", "Stacy Spensley", "CC BY 2.0", "https://www.flickr.com/photos/21001756@N06/4605791600"),
        Credit("mint", "Mint leaves", "ProFlowers.com", "CC BY 2.0", "https://www.flickr.com/photos/127365614@N08/33192314312"),
        Credit("rosemary", "Rosemary Herb Plant", "martinhoward", "CC BY 2.0", "https://www.flickr.com/photos/26752267@N00/6829036771"),
        Credit("garlic_powder", "garlic", "conskeptical", "CC BY-SA 2.0", "https://www.flickr.com/photos/32751486@N00/368883262"),
        Credit("nutmeg", "Nutmeg and Cumin -- HMM", "Thad Zajdowicz", "CC BY 2.0", "https://www.flickr.com/photos/40632439@N00/34083250855"),
    )
}
