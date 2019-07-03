#!/bin/bash
# Coded by ItHurtsLikeHell & Davoleo

for file in *_chest.json
do

    echo $file

OLD="chest"
NEW="chestplate"
#   Modifica delle string all'interno del file
#   sed "s/old_string/new_string/g" $file
#   remove -i to preview changes
    sed -i "s/$OLD/$NEW/g" $file

    wordToRemove="chest.json"
    wordToAdd="chestplate.json"

#   file = test_chest.json  ->  test_
    new=${file//$wordToRemove/}

#   file = test_    ->  test_chestplate.json
#   $nomevariabile1$nomevariabile2 = string contatenation
#   mv "vecchia string" "nuova string"  ->  file rename
    mv "$file" "$new$wordToAdd"

    echo LASAGNE!

done

# PAUSE function.
function pause() {
   read -p "$*"
}



pause 'Press [Enter] key to continue...'