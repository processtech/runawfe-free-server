export class Options {
    page: number;
    itemsPerPage: number;
    sortBy: string[];
    sortDesc: boolean[];
    groupBy: string[];
    groupDesc: boolean[];
    multiSort: boolean;
    mustSort: boolean;

    constructor() {
        this.page = 1;
        this.itemsPerPage = 10;
        this.sortBy = [];
        this.sortDesc = [];
        this.groupBy = [];
        this.groupDesc = [];
        this.multiSort = false;
        this.mustSort = true;
    }
}

export class Sorting {
    name: string;
    order: string;
    constructor(name: string, order: string = 'desc') {
        this.name = name;
        this.order = order;
    }
    static convert(sortBy: string[], sortDesc: boolean[]) {
        let listSortings: Sorting[] = [];
        for (let i=0; i < sortBy.length; i++) {
            listSortings.push(new Sorting(sortBy[i], sortDesc[i] ? 'desc' : 'asc'));
        }
        return listSortings;
    }
}
