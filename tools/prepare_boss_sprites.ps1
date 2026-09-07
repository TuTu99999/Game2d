Add-Type -AssemblyName System.Drawing

$processorSource = @'
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

public static class BossSpriteProcessor
{
    private const int FrameSize = 16;

    public static void Process(string sourcePath, string outputPath, int frameCount)
    {
        using (Bitmap source = new Bitmap(sourcePath))
        using (Bitmap transparent = RemoveConnectedLightBackground(source))
        using (Bitmap result = BuildSpriteSheet(transparent, frameCount))
        {
            result.Save(outputPath, ImageFormat.Png);
        }
    }

    private static Bitmap RemoveConnectedLightBackground(Bitmap source)
    {
        int width = source.Width;
        int height = source.Height;
        Bitmap image = new Bitmap(width, height, PixelFormat.Format32bppArgb);

        using (Graphics graphics = Graphics.FromImage(image))
        {
            graphics.DrawImageUnscaled(source, 0, 0);
        }

        Rectangle area = new Rectangle(0, 0, width, height);
        BitmapData data = image.LockBits(area, ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
        int stride = data.Stride;
        byte[] pixels = new byte[stride * height];
        Marshal.Copy(data.Scan0, pixels, 0, pixels.Length);

        bool[] background = new bool[width * height];
        Queue<int> queue = new Queue<int>();

        for (int x = 0; x < width; x++)
        {
            AddIfBackground(x, 0, width, height, stride, pixels, background, queue);
            AddIfBackground(x, height - 1, width, height, stride, pixels, background, queue);
        }

        for (int y = 0; y < height; y++)
        {
            AddIfBackground(0, y, width, height, stride, pixels, background, queue);
            AddIfBackground(width - 1, y, width, height, stride, pixels, background, queue);
        }

        while (queue.Count > 0)
        {
            int index = queue.Dequeue();
            int x = index % width;
            int y = index / width;

            AddIfBackground(x - 1, y, width, height, stride, pixels, background, queue);
            AddIfBackground(x + 1, y, width, height, stride, pixels, background, queue);
            AddIfBackground(x, y - 1, width, height, stride, pixels, background, queue);
            AddIfBackground(x, y + 1, width, height, stride, pixels, background, queue);
        }

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int index = y * width + x;
                int pixelIndex = y * stride + x * 4;
                pixels[pixelIndex + 3] = background[index] ? (byte)0 : (byte)255;
            }
        }

        Marshal.Copy(pixels, 0, data.Scan0, pixels.Length);
        image.UnlockBits(data);
        return image;
    }

    private static void AddIfBackground(
        int x,
        int y,
        int width,
        int height,
        int stride,
        byte[] pixels,
        bool[] background,
        Queue<int> queue)
    {
        if (x < 0 || y < 0 || x >= width || y >= height)
            return;

        int index = y * width + x;
        if (background[index])
            return;

        int pixelIndex = y * stride + x * 4;
        int blue = pixels[pixelIndex];
        int green = pixels[pixelIndex + 1];
        int red = pixels[pixelIndex + 2];
        int darkest = Math.Min(red, Math.Min(green, blue));
        int lightest = Math.Max(red, Math.Max(green, blue));

        bool isLightNeutral = darkest >= 220 && lightest - darkest <= 30;
        if (!isLightNeutral)
            return;

        background[index] = true;
        queue.Enqueue(index);
    }

    private static Bitmap BuildSpriteSheet(Bitmap source, int frameCount)
    {
        int sourceCellWidth = source.Width / frameCount;
        Rectangle[] bounds = new Rectangle[frameCount];
        int maxWidth = 1;
        int maxHeight = 1;

        for (int frame = 0; frame < frameCount; frame++)
        {
            bounds[frame] = FindFrameBounds(source, frame * sourceCellWidth, sourceCellWidth);
            maxWidth = Math.Max(maxWidth, bounds[frame].Width);
            maxHeight = Math.Max(maxHeight, bounds[frame].Height);
        }

        Bitmap result = new Bitmap(FrameSize * frameCount, FrameSize, PixelFormat.Format32bppArgb);
        using (Graphics graphics = Graphics.FromImage(result))
        {
            graphics.Clear(Color.Transparent);
            graphics.CompositingMode = CompositingMode.SourceCopy;
            graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
            graphics.PixelOffsetMode = PixelOffsetMode.Half;

            for (int frame = 0; frame < frameCount; frame++)
            {
                Rectangle sourceBounds = bounds[frame];
                // The original boss body is about 70x50 on a 96x96 game frame.
                // At source scale that is roughly 12x9 pixels on a 16x16 frame.
                int drawWidth = Math.Max(1, (int)Math.Round(sourceBounds.Width * 12.0 / maxWidth));
                int drawHeight = Math.Max(1, (int)Math.Round(sourceBounds.Height * 9.0 / maxHeight));
                int drawX = frame * FrameSize + (FrameSize - drawWidth) / 2;
                int drawY = FrameSize - drawHeight;
                Rectangle destination = new Rectangle(drawX, drawY, drawWidth, drawHeight);

                graphics.DrawImage(source, destination, sourceBounds, GraphicsUnit.Pixel);
            }
        }

        return result;
    }

    private static Rectangle FindFrameBounds(Bitmap image, int startX, int cellWidth)
    {
        int minX = startX + cellWidth;
        int minY = image.Height;
        int maxX = startX;
        int maxY = 0;

        for (int y = 0; y < image.Height; y++)
        {
            for (int x = startX; x < startX + cellWidth; x++)
            {
                if (image.GetPixel(x, y).A == 0)
                    continue;

                minX = Math.Min(minX, x);
                minY = Math.Min(minY, y);
                maxX = Math.Max(maxX, x);
                maxY = Math.Max(maxY, y);
            }
        }

        if (minX > maxX || minY > maxY)
            return new Rectangle(startX, 0, 1, 1);

        return Rectangle.FromLTRB(minX, minY, maxX + 1, maxY + 1);
    }
}
'@

Add-Type -TypeDefinition $processorSource -ReferencedAssemblies System.Drawing

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$generatedFolder = Join-Path $projectRoot 'art\generated'
$drawableFolder = Join-Path $projectRoot 'app\src\main\res\drawable'

[BossSpriteProcessor]::Process(
    (Join-Path $generatedFolder 'bossdeath_generated.png'),
    (Join-Path $drawableFolder 'bossdeath.png'),
    6
)

[BossSpriteProcessor]::Process(
    (Join-Path $generatedFolder 'bosshurt_generated.png'),
    (Join-Path $drawableFolder 'bosshurt.png'),
    4
)

Write-Output 'Created bossdeath.png (6 frames) and bosshurt.png (4 frames).'
