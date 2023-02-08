/* license: https://mit-license.org
 * ==============================================================================
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Albert Moky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ==============================================================================
 */
package chat.dim.utils;

import java.util.Random;

public enum Nickname {

    INSTANCE;

    public static Nickname getInstance() {
        return INSTANCE;
    }

    /**
     *  Generate english name
     *
     * @return given name and family name
     */
    public String english() {
        if (nextInt(1, 5) == 1) {
            return girl() + " " + family();
        } else {
            return boy() + " " + family();
        }
    }

    public String boy() {
        return nextName(BOY_NAMES);
    }
    public String girl() {
        return nextName(GIRL_NAMES);
    }
    public String family() {
        return nextName(FAMILY_NAMES);
    }

    String nextName(String[] names) {
        return names[nextInt(0, names.length)];
    }

    private int nextInt(int start, int end) {
        int next = random.nextInt();
        if (next < 0) {
            next = - next;
        }
        return next % (end - start) + start;
    }
    private final Random random = new Random();

    String[] BOY_NAMES = {
            "Aaron", "Alan", "Alex", "Alvin", "Andrew", "Andy",
            "Ben", "Benson", "Bill", "Bob", "Bobby", "Brian", "Bruce",
            "Carl", "Caspar", "Charles", "Chris", "Clark", "Cole",
            "Daniel", "Denny", "David", "Dennis", "Douglas", "Dylan",
            "Edison", "Edward", "Eric", "Ford", "Frank", "Gary", "Gavin", "George",
            "Hank", "Harrison", "Harry", "Henry", "Howard", "Hugo", "Isaac", "Ivan",
            "Jack", "James", "Jason", "Jerry", "Jim", "John", "Jordan", "Justin",
            "Ken", "Kevin", "Larry", "Leo", "Leonard", "Louis", "Luke",
            "Marcus", "Mark", "Martin", "Marvin", "Matthew", "Max", "Michael", "Mike",
            "Neil", "Nelson", "Nicholas", "Nick",
            "Oscar", "Owen", "Paul", "Peter", "Philip", "Quentin",
            "Ray", "Raymond", "Richard", "Robert", "Robin", "Rock", "Roger", "Roy",
            "Sam", "Samuel", "Scott", "Simon", "Stanley", "Steve", "Steven",
            "Terry", "Ted", "Thomas", "Tim", "Todd", "Tommy", "Tom", "Tony", "Tyler",
            "Ulysses", "Victor", "Vincent", "Warner", "Warren", "Wayne",
            "Xenophon", "Zack",
    };
    String[] GIRL_NAMES = {
            "Abby", "Ada", "Alice", "Alina", "Amanda", "Amy", "Angel", "Ann", "Audrey",
            "Becky", "Bella", "Betty", "Bonnie", "Britney",
            "Carry", "Cassie", "Catherine", "Cathy", "Charlene", "Christina", "Cindy",
            "Daisy", "Diana", "Doris", "Ella", "Ellie", "Emily", "Eva", "Eve",
            "Fanny", "Fiona", "Gina", "Grace",
            "Helena", "Isabella", "Irene", "Iris", "Ivy",
            "Jane", "Jean", "Jennifer", "Jenny", "Jessica", "Jessie", "Joanna", "Judy",
            "Karen", "Katherine", "Kate", "Kathy", "Katrina", "Kelly",
            "Laura", "Lena", "Lillian", "Lily", "Linda", "Lisa", "Lucia", "Lucy", "Lydia",
            "Maggie", "Manda", "Margaret", "Mary", "May", "Melody", "Mia", "Milly", "Monica",
            "Nancy", "Natasha", "Nicole", "Nina",
            "Olivia", "Pamela", "Peggy", "Polly", "Rachel", "Rita", "Rose",
            "Sabrina", "Sally", "Sandy", "Sarah", "Selina", "Shirley", "Sophia", "Susan",
            "Tammy", "Tiffany", "Tina", "Tracy",
            "Ursula", "Vicky", "Victoria", "Vivian", "Wendy", "Winnie",
            "Yolanda", "Zoe",
    };

    String[] FAMILY_NAMES = {
            "Brown", "Davis", "Evans", "Garcia",
            "Johnson", "Jones", "Miller",
            "Rodriguez", "Smith", "Taylor", "Thomas",
            "Williams", "Wilson",

            "Back", "Baker", "Bird", "Brain", "Brook", "Bull",
            "Churchill", "Clinton", "Cook", "Cotton",
            "David", "Finger", "Fox", "George", "Green",
            "Hall", "Hand", "Hawk", "Henry", "Hill", "Hunter", "London", "Macadam", "Mill",
            "Reed", "Sharp", "Stock", "Turner",
            "Well", "White", "Wood",
    };
}
