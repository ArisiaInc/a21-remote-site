import { Time } from '@angular/common'

import { ProgramPerson } from "@app/_models";

export class ProgramItem {
    id: string
    title: string
    tags: string[]
    date: string
    time: string
    mins: string
    loc: string[]
    people: object[]
    desc: string
}