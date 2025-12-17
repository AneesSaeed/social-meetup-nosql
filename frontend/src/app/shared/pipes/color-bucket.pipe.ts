import { Pipe, PipeTransform } from "@angular/core";
import { colorBucket } from "../utils/colors-hash";

@Pipe({ name: 'colorBucket'})
export class ColorBucketPipe implements PipeTransform {
  transform(value: unknown, mod = 8): number {
    return colorBucket(value, mod);
  }
}
